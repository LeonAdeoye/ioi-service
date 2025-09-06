package com.leon.controller

import com.leon.model.IoiRequest
import com.leon.model.IoiResponse
import com.leon.service.RulesEngine
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/ioi")
class IoiController(private val rulesEngine: RulesEngine)
{
    @PostMapping("/process")
    fun processIoi(@RequestBody request: IoiRequest): IoiResponse
    {
        return rulesEngine.processIoiRequest(request)
    }
}