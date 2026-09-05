package com.leon.service

import com.leon.model.IoiCount
import com.leon.model.IoiFailure
import com.leon.model.IoiRequest
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

@Service
class IoiStatsService
{
    private val totalCreated = AtomicLong(0)
    private val totalUnapproved = AtomicLong(0)
    private val createdByTrader = ConcurrentHashMap<String, AtomicLong>()
    private val createdByStock = ConcurrentHashMap<String, AtomicLong>()
    private val createdByMarket = ConcurrentHashMap<String, AtomicLong>()
    private val unapprovedByTrader = ConcurrentHashMap<String, AtomicLong>()
    private val unapprovedByStock = ConcurrentHashMap<String, AtomicLong>()
    private val unapprovedByMarket = ConcurrentHashMap<String, AtomicLong>()
    private val unapprovedByReason = ConcurrentHashMap<String, AtomicLong>()
    private val failures = ConcurrentLinkedQueue<IoiFailure>()

    fun recordCreated(request: IoiRequest)
    {
        totalCreated.incrementAndGet()
        increment(createdByTrader, request.trader)
        increment(createdByStock, request.ric)
        increment(createdByMarket, request.originalMarket)
    }

    fun recordUnapproved(request: IoiRequest, reason: String, lastPrice: Double? = null, advPercentage: Double? = null)
    {
        totalUnapproved.incrementAndGet()
        increment(unapprovedByTrader, request.trader)
        increment(unapprovedByStock, request.ric)
        increment(unapprovedByMarket, request.originalMarket)
        increment(unapprovedByReason, reason)
        failures.add(
            IoiFailure(
                requestId = request.requestId.toString(),
                trader = request.trader,
                ric = request.ric,
                originalMarket = request.originalMarket,
                reason = reason,
                timestamp = request.timestamp,
                source = request.source,
                quantity = request.quantity,
                price = request.price,
                lastPrice = lastPrice,
                advPercentage = advPercentage
            )
        )
    }

    fun getCreatedTotal(): IoiCount
    {
        return IoiCount(totalCreated.get())
    }

    fun getCreatedByTrader(): Map<String, Long>
    {
        return snapshot(createdByTrader)
    }

    fun getCreatedByStock(): Map<String, Long>
    {
        return snapshot(createdByStock)
    }

    fun getCreatedByMarket(): Map<String, Long>
    {
        return snapshot(createdByMarket)
    }

    fun getUnapprovedTotal(): IoiCount
    {
        return IoiCount(totalUnapproved.get())
    }

    fun getUnapprovedByTrader(): Map<String, Long>
    {
        return snapshot(unapprovedByTrader)
    }

    fun getUnapprovedByStock(): Map<String, Long>
    {
        return snapshot(unapprovedByStock)
    }

    fun getUnapprovedByMarket(): Map<String, Long>
    {
        return snapshot(unapprovedByMarket)
    }

    fun getUnapprovedByReason(): Map<String, Long>
    {
        return snapshot(unapprovedByReason).filterKeys { !it.startsWith(BLOCKED_REASON_PREFIX) }
    }

    fun getFailures(): List<IoiFailure>
    {
        return failures.filter { !it.reason.startsWith(BLOCKED_REASON_PREFIX) }
    }

    private fun increment(map: ConcurrentHashMap<String, AtomicLong>, key: String)
    {
        map.computeIfAbsent(key) { AtomicLong(0) }.incrementAndGet()
    }

    private fun snapshot(map: ConcurrentHashMap<String, AtomicLong>): Map<String, Long>
    {
        return map.mapValues { it.value.get() }
    }

    companion object
    {
        private const val BLOCKED_REASON_PREFIX = "Blocked:"
    }
}
