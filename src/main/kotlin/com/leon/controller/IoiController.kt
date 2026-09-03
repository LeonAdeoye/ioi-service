package com.leon.controller

import com.leon.config.IoiProperties
import com.leon.config.RuleThresholds
import com.leon.model.IoiBulkAccepted
import com.leon.model.IoiRequest
import com.leon.model.IoiResponse
import com.leon.service.IoiIngestService
import com.leon.service.IoiRuleConfigService
import com.leon.service.RulesEngine
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ioi")
class IoiController(
    private val rulesEngine: RulesEngine,
    private val ioiIngestService: IoiIngestService,
    private val ioiRuleConfigService: IoiRuleConfigService,
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
        val accepted = ioiIngestService.enqueueAll(requests)
        return IoiBulkAccepted(accepted)
    }

    @GetMapping("/reconfigure")
    fun reconfigure(): RuleThresholds
    {
        logger.info("Received request to reconfigure IOI rule thresholds")
        ioiRuleConfigService.reconfigure()
        return properties.rules
    }
}
