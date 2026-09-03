package com.leon.controller

import com.leon.model.IoiCount
import com.leon.model.IoiFailure
import com.leon.service.IoiStatsService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ioi")
class IoiStatsController(private val ioiStatsService: IoiStatsService)
{
    @GetMapping("/stats/created")
    fun createdTotal(): IoiCount
    {
        return ioiStatsService.getCreatedTotal()
    }

    @GetMapping("/stats/created/trader")
    fun createdByTrader(): Map<String, Long>
    {
        return ioiStatsService.getCreatedByTrader()
    }

    @GetMapping("/stats/created/stock")
    fun createdByStock(): Map<String, Long>
    {
        return ioiStatsService.getCreatedByStock()
    }

    @GetMapping("/stats/created/market")
    fun createdByMarket(): Map<String, Long>
    {
        return ioiStatsService.getCreatedByMarket()
    }

    @GetMapping("/stats/unapproved")
    fun unapprovedTotal(): IoiCount
    {
        return ioiStatsService.getUnapprovedTotal()
    }

    @GetMapping("/stats/unapproved/trader")
    fun unapprovedByTrader(): Map<String, Long>
    {
        return ioiStatsService.getUnapprovedByTrader()
    }

    @GetMapping("/stats/unapproved/stock")
    fun unapprovedByStock(): Map<String, Long>
    {
        return ioiStatsService.getUnapprovedByStock()
    }

    @GetMapping("/stats/unapproved/market")
    fun unapprovedByMarket(): Map<String, Long>
    {
        return ioiStatsService.getUnapprovedByMarket()
    }

    @GetMapping("/stats/unapproved/reason")
    fun unapprovedByReason(): Map<String, Long>
    {
        return ioiStatsService.getUnapprovedByReason()
    }

    @GetMapping("/failures")
    fun failures(): List<IoiFailure>
    {
        return ioiStatsService.getFailures()
    }
}
