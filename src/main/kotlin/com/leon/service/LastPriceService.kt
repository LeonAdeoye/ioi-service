package com.leon.service

import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Service
class LastPriceService(private val marketDataClient: MarketDataClient)
{
    private val logger = LoggerFactory.getLogger(LastPriceService::class.java)
    private val lastPrices = ConcurrentHashMap<String, Double>()
    private val liveCounts = ConcurrentHashMap<String, AtomicInteger>()
    private val trackedRics = ConcurrentHashMap.newKeySet<String>()

    fun ensurePrice(ric: String): Double?
    {
        if (trackedRics.add(ric))
        {
            marketDataClient.subscribe(ric)
            val snapshot = marketDataClient.getLastPrice(ric)
            if (snapshot != null)
                lastPrices[ric] = snapshot
        }

        return lastPrices[ric]
    }

    fun getCached(ric: String): Double?
    {
        return lastPrices[ric]
    }

    fun isTracking(ric: String): Boolean
    {
        return trackedRics.contains(ric)
    }

    fun updateTick(ric: String, price: Double)
    {
        if (!trackedRics.contains(ric))
            return

        lastPrices[ric] = price
    }

    fun onLiveAdded(ric: String)
    {
        liveCounts.computeIfAbsent(ric) { AtomicInteger(0) }.incrementAndGet()
        ensurePrice(ric)
    }

    fun onLiveRemoved(ric: String)
    {
        val remaining = liveCounts.compute(ric) { _, counter ->
            if (counter == null)
                return@compute null
            val next = counter.decrementAndGet()
            if (next > 0) counter else null
        }
        if (remaining == null)
            stopTracking(ric)
    }

    fun releaseIfUnused(ric: String)
    {
        val live = liveCounts[ric]?.get() ?: 0
        if (live <= 0)
            stopTracking(ric)
    }

    @PreDestroy
    fun shutdown()
    {
        trackedRics.toList().forEach { stopTracking(it) }
    }

    private fun stopTracking(ric: String)
    {
        if (!trackedRics.remove(ric))
            return

        lastPrices.remove(ric)
        liveCounts.remove(ric)
        marketDataClient.unsubscribe(ric)
        logger.info("Stopped last-price tracking for {}", ric)
    }
}
