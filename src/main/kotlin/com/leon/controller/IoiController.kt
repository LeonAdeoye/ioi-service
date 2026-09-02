package com.leon.controller

import com.leon.model.IoiRequest
import com.leon.model.IoiResponse
import com.leon.service.RulesEngine
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/ioi")
class IoiController(private val rulesEngine: RulesEngine)
{
    private val logger = LoggerFactory.getLogger(IoiController::class.java)

    @PostMapping("/process")
    fun processIoi(@RequestBody request: IoiRequest): IoiResponse
    {
        logger.info("Received IOI process request {}", request.requestId)
        return rulesEngine.processIoiRequest(request)
    }
}
