package com.leon.service

import com.leon.model.IoiBlock
import com.leon.model.IoiBlockedFailure
import com.leon.model.IoiFailure
import com.leon.model.IoiRequest
import com.leon.repository.IoiBlockRepository
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicLong

@Service
class IoiBlockingService(private val ioiLifecycleService: IoiLifecycleService, private val ioiBlockRepository: IoiBlockRepository)
{
    private val logger = LoggerFactory.getLogger(IoiBlockingService::class.java)
    private val blockedTraders = ConcurrentHashMap<String, IoiBlock>()
    private val blockedStocks = ConcurrentHashMap<String, IoiBlock>()
    private val blockedMarkets = ConcurrentHashMap<String, IoiBlock>()

    private val blockedByTraderCount = ConcurrentHashMap<String, AtomicLong>()
    private val blockedByStockCount = ConcurrentHashMap<String, AtomicLong>()
    private val blockedByMarketCount = ConcurrentHashMap<String, AtomicLong>()
    private val blockedByTraderFailures = ConcurrentLinkedQueue<IoiFailure>()
    private val blockedByStockFailures = ConcurrentLinkedQueue<IoiFailure>()
    private val blockedByMarketFailures = ConcurrentLinkedQueue<IoiFailure>()

    @PostConstruct
    fun loadPersistedBlocks()
    {
        try
        {
            val blocks = ioiBlockRepository.findAll()
            blocks.forEach { putInCache(it) }
            logger.info("Loaded {} IOI blocks from MongoDB", blocks.size)
        }
        catch (e: Exception)
        {
            logger.warn("Failed to load IOI blocks from MongoDB: {}", e.message)
        }
    }

    fun blockTrader(trader: String, userId: String)
    {
        blockedTraders[trader] = persistBlock("TRADER", trader, userId)
        val cancelled = ioiLifecycleService.cancelMatching { it.trader == trader }
        cancelled.forEach { recordBlockedTrader(it, "Blocked: trader '${trader}' is blocked") }
        logger.info("Blocked trader: {}; blocked {} live IOIs", trader, cancelled.size)
    }

    fun unblockTrader(trader: String)
    {
        blockedTraders.remove(trader)
        deletePersisted("TRADER", trader)
        logger.info("Unblocked trader: {}", trader)
    }

    fun blockStock(stock: String, userId: String)
    {
        blockedStocks[stock] = persistBlock("STOCK", stock, userId)
        val cancelled = ioiLifecycleService.cancelMatching { it.ric == stock }
        cancelled.forEach { recordBlockedStock(it, "Blocked: stock '${stock}' is blocked") }
        logger.info("Blocked stock: {}; blocked {} live IOIs", stock, cancelled.size)
    }

    fun unblockStock(stock: String)
    {
        blockedStocks.remove(stock)
        deletePersisted("STOCK", stock)
        logger.info("Unblocked stock: {}", stock)
    }

    fun blockMarket(market: String, userId: String)
    {
        blockedMarkets[market] = persistBlock("MARKET", market, userId)
        val cancelled = ioiLifecycleService.cancelMatching { it.originalMarket == market }
        cancelled.forEach { recordBlockedMarket(it, "Blocked: market '${market}' is blocked") }
        logger.info("Blocked market: {}; blocked {} live IOIs", market, cancelled.size)
    }

    fun unblockMarket(market: String)
    {
        blockedMarkets.remove(market)
        deletePersisted("MARKET", market)
        logger.info("Unblocked market: {}", market)
    }

    fun getBlockedTraders(): Set<String>
    {
        return blockedTraders.keys.toSet()
    }

    fun getBlockedStocks(): Set<String>
    {
        return blockedStocks.keys.toSet()
    }

    fun getBlockedMarkets(): Set<String>
    {
        return blockedMarkets.keys.toSet()
    }

    fun getAllBlocks(): List<IoiBlock>
    {
        return blockedTraders.values + blockedStocks.values + blockedMarkets.values
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

    fun getAllBlockedFailures(): List<IoiBlockedFailure>
    {
        return blockedByTraderFailures.map { toBlocked(it, "TRADER") } +
            blockedByStockFailures.map { toBlocked(it, "STOCK") } +
            blockedByMarketFailures.map { toBlocked(it, "MARKET") }
    }

    private fun persistBlock(blockType: String, value: String, userId: String): IoiBlock
    {
        val block = IoiBlock.of(blockType, value, userId)
        try
        {
            return ioiBlockRepository.save(block)
        }
        catch (e: Exception)
        {
            logger.error("Failed to persist IOI block {} {}: {}", blockType, value, e.message)
            return block
        }
    }

    private fun deletePersisted(blockType: String, value: String)
    {
        try
        {
            ioiBlockRepository.deleteById("$blockType:$value")
        }
        catch (e: Exception)
        {
            logger.error("Failed to delete persisted IOI block {} {}: {}", blockType, value, e.message)
        }
    }

    private fun putInCache(block: IoiBlock)
    {
        when (block.blockType)
        {
            "TRADER" -> blockedTraders[block.value] = block
            "STOCK" -> blockedStocks[block.value] = block
            "MARKET" -> blockedMarkets[block.value] = block
            else -> logger.warn("Ignoring persisted IOI block with unknown type: {}", block.blockType)
        }
    }

    private fun toBlocked(failure: IoiFailure, blockType: String): IoiBlockedFailure
    {
        return IoiBlockedFailure(
            requestId = failure.requestId,
            trader = failure.trader,
            ric = failure.ric,
            originalMarket = failure.originalMarket,
            reason = failure.reason,
            timestamp = failure.timestamp,
            source = failure.source,
            blockType = blockType
        )
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
                source = request.source,
                quantity = request.quantity,
                price = request.price
            )
        )
        logger.info("IOI request {} blocked: {}", request.requestId, reason)
    }
}
