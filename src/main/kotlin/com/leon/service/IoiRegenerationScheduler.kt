package com.leon.service

import com.leon.config.IoiProperties
import com.leon.model.IoiRegenerationAction
import com.leon.model.IoiRegenerationCommand
import com.leon.model.LiveIoi
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

@Service
@DependsOn("ioiRegenerationDisruptorStarter")
class IoiRegenerationScheduler(
    private val properties: IoiProperties,
    private val liveIoiRegistry: LiveIoiRegistry,
    private val marketHoursService: MarketHoursService,
    private val ioiQuantityDecayService: IoiQuantityDecayService,
    private val ioiRegenerationService: IoiRegenerationService
)
{
    private val logger = LoggerFactory.getLogger(IoiRegenerationScheduler::class.java)
    private val executor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "ioi-regen-scheduler").apply { isDaemon = true }
    }
    private var scheduledFuture: ScheduledFuture<*>? = null

    @PostConstruct
    fun start()
    {
        reschedule()
    }

    @Synchronized
    fun reschedule()
    {
        scheduledFuture?.cancel(false)
        val interval = properties.regenerationIntervalSeconds.coerceAtLeast(1L)
        scheduledFuture = executor.scheduleAtFixedRate({ tick() }, interval, interval, TimeUnit.SECONDS)
        logger.info("IOI regeneration scheduler interval set to {} second(s)", interval)
    }

    private fun tick()
    {
        try
        {
            val now = System.currentTimeMillis()
            val live = liveIoiRegistry.snapshotLive()
            val pending = liveIoiRegistry.snapshotPendingAfterLunch()
            val markets = (live + pending).map { it.request.originalMarket }.toSet()

            markets.forEach { market ->
                processMarket(market, now, live, pending)
            }
        }
        catch (e: Exception)
        {
            logger.error("IOI regeneration scheduler tick failed", e)
        }
    }

    private fun processMarket(market: String, now: Long, live: List<LiveIoi>, pending: List<LiveIoi>)
    {
        val liveForMarket = live.filter { it.request.originalMarket.equals(market, ignoreCase = true) }
        val pendingForMarket = pending.filter { it.request.originalMarket.equals(market, ignoreCase = true) }

        if (marketHoursService.isAfterHours(market, now))
        {
            liveForMarket.forEach { ioi ->
                liveIoiRegistry.removeLive(ioi.request.requestId)
                ioiRegenerationService.enqueue(IoiRegenerationCommand(IoiRegenerationAction.CANCEL_ONLY, ioi.request, ioi.originalQuantity))
            }
            pendingForMarket.forEach { ioi ->
                liveIoiRegistry.removePendingAfterLunch(ioi.request.requestId)
            }
            if (liveForMarket.isNotEmpty() || pendingForMarket.isNotEmpty())
                logger.info("Cancelled {} live IOI(s) and dropped {} pending lunch IOI(s) for closed market {}", liveForMarket.size, pendingForMarket.size, market)
            return
        }

        if (marketHoursService.isDuringLunch(market, now))
        {
            liveForMarket.forEach { ioi ->
                liveIoiRegistry.moveLiveToPendingAfterLunch(ioi)
                ioiRegenerationService.enqueue(IoiRegenerationCommand(IoiRegenerationAction.CANCEL_ONLY, ioi.request, ioi.originalQuantity))
            }
            if (liveForMarket.isNotEmpty())
                logger.info("Cancelled {} IOI(s) for lunch on market {}; will recreate after lunch", liveForMarket.size, market)
            return
        }

        pendingForMarket.forEach { ioi ->
            liveIoiRegistry.removePendingAfterLunch(ioi.request.requestId)
            ioiRegenerationService.enqueue(
                IoiRegenerationCommand(
                    action = IoiRegenerationAction.RECREATE_ONLY,
                    request = ioi.request,
                    originalQuantity = ioi.originalQuantity,
                    nextQuantity = ioi.request.quantity
                )
            )
        }

        liveForMarket.forEach { ioi ->
            if (now < ioi.expiresAtMillis)
                return@forEach

            liveIoiRegistry.removeLive(ioi.request.requestId)
            val lifetime = ioi.request.lifeTimeInMinutes ?: properties.defaultLifetimeMinutes
            val openMinutes = marketHoursService.openMinutes(market)
            val nextQuantity = ioiQuantityDecayService.nextQuantity(ioi.originalQuantity, ioi.request.quantity, lifetime, openMinutes)
            if (nextQuantity <= 0L)
            {
                ioiRegenerationService.enqueue(IoiRegenerationCommand(IoiRegenerationAction.CANCEL_ONLY, ioi.request, ioi.originalQuantity))
                logger.info("Cancelled IOI {} at expiry with zero remaining quantity", ioi.request.requestId)
            }
            else
            {
                ioiRegenerationService.enqueue(
                    IoiRegenerationCommand(
                        action = IoiRegenerationAction.CANCEL_AND_RECREATE,
                        request = ioi.request,
                        originalQuantity = ioi.originalQuantity,
                        nextQuantity = nextQuantity
                    )
                )
                logger.info("Cancelled IOI {} at expiry; recreating with quantity {}", ioi.request.requestId, nextQuantity)
            }
        }
    }

    @PreDestroy
    fun stop()
    {
        scheduledFuture?.cancel(false)
        executor.shutdownNow()
        logger.info("IOI regeneration scheduler stopped")
    }
}
