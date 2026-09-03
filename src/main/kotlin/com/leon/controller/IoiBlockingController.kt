package com.leon.controller

import com.leon.model.IoiBlockedFailure
import com.leon.model.IoiFailure
import com.leon.service.IoiBlockingService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ioi/block")
class IoiBlockingController(private val ioiBlockingService: IoiBlockingService)
{
    private val logger = LoggerFactory.getLogger(IoiBlockingController::class.java)

    @PostMapping("/trader")
    fun blockTrader(@RequestParam trader: String)
    {
        logger.info("Received request to block trader: {}", trader)
        ioiBlockingService.blockTrader(trader)
    }

    @DeleteMapping("/trader")
    fun unblockTrader(@RequestParam trader: String)
    {
        logger.info("Received request to unblock trader: {}", trader)
        ioiBlockingService.unblockTrader(trader)
    }

    @PostMapping("/stock")
    fun blockStock(@RequestParam stock: String)
    {
        logger.info("Received request to block stock: {}", stock)
        ioiBlockingService.blockStock(stock)
    }

    @DeleteMapping("/stock")
    fun unblockStock(@RequestParam stock: String)
    {
        logger.info("Received request to unblock stock: {}", stock)
        ioiBlockingService.unblockStock(stock)
    }

    @PostMapping("/market")
    fun blockMarket(@RequestParam market: String)
    {
        logger.info("Received request to block market: {}", market)
        ioiBlockingService.blockMarket(market)
    }

    @DeleteMapping("/market")
    fun unblockMarket(@RequestParam market: String)
    {
        logger.info("Received request to unblock market: {}", market)
        ioiBlockingService.unblockMarket(market)
    }

    @GetMapping("/traders")
    fun getBlockedTraders(): Set<String>
    {
        return ioiBlockingService.getBlockedTraders()
    }

    @GetMapping("/stocks")
    fun getBlockedStocks(): Set<String>
    {
        return ioiBlockingService.getBlockedStocks()
    }

    @GetMapping("/markets")
    fun getBlockedMarkets(): Set<String>
    {
        return ioiBlockingService.getBlockedMarkets()
    }

    @GetMapping("/stats/trader")
    fun blockedByTraderCount(): Map<String, Long>
    {
        return ioiBlockingService.getBlockedByTraderCount()
    }

    @GetMapping("/stats/stock")
    fun blockedByStockCount(): Map<String, Long>
    {
        return ioiBlockingService.getBlockedByStockCount()
    }

    @GetMapping("/stats/market")
    fun blockedByMarketCount(): Map<String, Long>
    {
        return ioiBlockingService.getBlockedByMarketCount()
    }

    @GetMapping("/failures")
    fun blockedFailures(): List<IoiBlockedFailure>
    {
        return ioiBlockingService.getAllBlockedFailures()
    }

    @GetMapping("/failures/trader")
    fun blockedByTraderFailures(): List<IoiFailure>
    {
        return ioiBlockingService.getBlockedByTraderFailures()
    }

    @GetMapping("/failures/stock")
    fun blockedByStockFailures(): List<IoiFailure>
    {
        return ioiBlockingService.getBlockedByStockFailures()
    }

    @GetMapping("/failures/market")
    fun blockedByMarketFailures(): List<IoiFailure>
    {
        return ioiBlockingService.getBlockedByMarketFailures()
    }
}
