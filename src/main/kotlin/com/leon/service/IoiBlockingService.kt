package com.leon.service

import com.leon.model.IoiFailure
import com.leon.model.IoiRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

@Service
class IoiBlockingService
{
    private val logger = LoggerFactory.getLogger(IoiBlockingService::class.java)
    private val blockedTraders = ConcurrentHashMap.newKeySet<String>()
    private val blockedStocks = ConcurrentHashMap.newKeySet<String>()
    private val blockedMarkets = ConcurrentHashMap.newKeySet<String>()

    private val blockedByTraderCount = ConcurrentHashMap<String, AtomicLong>()
    private val blockedByStockCount = ConcurrentHashMap<String, AtomicLong>()
    private val blockedByMarketCount = ConcurrentHashMap<String, AtomicLong>()
    private val blockedByTraderFailures = ConcurrentLinkedQueue<IoiFailure>()
    private val blockedByStockFailures = ConcurrentLinkedQueue<IoiFailure>()
    private val blockedByMarketFailures = ConcurrentLinkedQueue<IoiFailure>()

    fun blockTrader(trader: String)
    {
        blockedTraders.add(trader)
        logger.info("Blocked trader: {}", trader)
    }

    fun unblockTrader(trader: String)
    {
        blockedTraders.remove(trader)
        logger.info("Unblocked trader: {}", trader)
    }

    fun blockStock(stock: String)
    {
        blockedStocks.add(stock)
        logger.info("Blocked stock: {}", stock)
    }

    fun unblockStock(stock: String)
    {
        blockedStocks.remove(stock)
        logger.info("Unblocked stock: {}", stock)
    }

    fun blockMarket(market: String)
    {
        blockedMarkets.add(market)
        logger.info("Blocked market: {}", market)
    }

    fun unblockMarket(market: String)
    {
        blockedMarkets.remove(market)
        logger.info("Unblocked market: {}", market)
    }

    fun getBlockedTraders(): Set<String>
    {
        return blockedTraders.toSet()
    }

    fun getBlockedStocks(): Set<String>
    {
        return blockedStocks.toSet()
    }

    fun getBlockedMarkets(): Set<String>
    {
        return blockedMarkets.toSet()
    }

    fun recordBlockedTrader(request: IoiRequest, reason: String)
    {
        recordBlocked(request, reason, blockedByTraderCount, request.trader, blockedByTraderFailures)
    }

    fun recordBlockedStock(request: IoiRequest, reason: String)
    {
        recordBlocked(request, reason, blockedByStockCount, request.ric, blockedByStockFailures)
    }

    fun recordBlockedMarket(request: IoiRequest, reason: String)
    {
        recordBlocked(request, reason, blockedByMarketCount, request.originalMarket, blockedByMarketFailures)
    }

    fun getBlockedByTraderCount(): Map<String, Long>
    {
        return blockedByTraderCount.mapValues { it.value.get() }
    }

    fun getBlockedByStockCount(): Map<String, Long>
    {
        return blockedByStockCount.mapValues { it.value.get() }
    }

    fun getBlockedByMarketCount(): Map<String, Long>
    {
        return blockedByMarketCount.mapValues { it.value.get() }
    }

    fun getBlockedByTraderFailures(): List<IoiFailure>
    {
        return blockedByTraderFailures.toList()
    }

    fun getBlockedByStockFailures(): List<IoiFailure>
    {
        return blockedByStockFailures.toList()
    }

    fun getBlockedByMarketFailures(): List<IoiFailure>
    {
        return blockedByMarketFailures.toList()
    }

    private fun recordBlocked(
        request: IoiRequest,
        reason: String,
        countMap: ConcurrentHashMap<String, AtomicLong>,
        key: String,
        failureQueue: ConcurrentLinkedQueue<IoiFailure>
    )
    {
        countMap.computeIfAbsent(key) { AtomicLong(0) }.incrementAndGet()
        failureQueue.add(
            IoiFailure(
                requestId = request.requestId.toString(),
                trader = request.trader,
                ric = request.ric,
                originalMarket = request.originalMarket,
                reason = reason,
                timestamp = request.timestamp,
                source = request.source
            )
        )
        logger.info("IOI request {} blocked: {}", request.requestId, reason)
    }
}
