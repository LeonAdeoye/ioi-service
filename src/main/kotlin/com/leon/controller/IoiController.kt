package com.leon.controller

import com.leon.config.IoiProperties
import com.leon.model.Ioi
import com.leon.model.IoiBulkAccepted
import com.leon.model.IoiCount
import com.leon.model.IoiRequest
import com.leon.model.IoiResponse
import com.leon.model.IoiRuntimeConfig
import com.leon.service.IoiBookService
import com.leon.service.IoiIngestService
import com.leon.service.IoiLifecycleService
import com.leon.service.IoiRuleConfigService
import com.leon.service.RulesEngine
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/ioi")
class IoiController(
    private val rulesEngine: RulesEngine,
    private val ioiIngestService: IoiIngestService,
    private val ioiRuleConfigService: IoiRuleConfigService,
    private val ioiBookService: IoiBookService,
    private val ioiLifecycleService: IoiLifecycleService,
    private val properties: IoiProperties
)
{
    private val logger = LoggerFactory.getLogger(IoiController::class.java)

    @PostMapping("/process")
    fun processIoi(@RequestBody request: IoiRequest): IoiResponse
    {
        logger.info("Received IOI process request {}", request.requestId)
        return rulesEngine.processIoiRequest(request)
    }

    @PostMapping("/process/bulk")
    fun processBulkIois(@RequestBody requests: List<IoiRequest>): IoiBulkAccepted
    {
        logger.info("Received bulk IOI process request with {} item(s)", requests.size)
        val accepted = ioiIngestService.enqueueAll(requests, "REST")
        return IoiBulkAccepted(accepted)
    }

    @GetMapping("/created")
    fun getCreatedIois(): List<Ioi>
    {
        return ioiBookService.getLive()
    }

    @GetMapping("/live")
    fun getLiveIois(): List<Ioi>
    {
        return ioiBookService.getLive()
    }

    @GetMapping("/cancelled")
    fun getCancelledIois(): List<Ioi>
    {
        return ioiBookService.getManuallyCancelled()
    }

    @DeleteMapping("/all")
    fun deleteAllIois(): IoiCount
    {
        logger.info("Received request to delete all IOIs")
        val cancelled = ioiLifecycleService.deleteAll()
        return IoiCount(cancelled.toLong())
    }

    @DeleteMapping("/{requestId}")
    fun cancelIoi(@PathVariable requestId: UUID): IoiResponse
    {
        logger.info("Received request to cancel IOI {}", requestId)
        val cancelled = ioiLifecycleService.cancel(requestId, true)
        return IoiResponse(
            requestId = requestId.toString(),
            approved = cancelled,
            reason = if (cancelled) null else "IOI not found"
        )
    }

    @GetMapping("/reconfigure")
    fun reconfigure(): IoiRuntimeConfig
    {
        logger.info("Received request to reconfigure IOI rule thresholds and regeneration settings")
        ioiRuleConfigService.reconfigure()
        return IoiRuntimeConfig(
            rules = properties.rules,
            defaultLifetimeMinutes = properties.defaultLifetimeMinutes,
            regenerationIntervalSeconds = properties.regenerationIntervalSeconds
        )
    }
}
