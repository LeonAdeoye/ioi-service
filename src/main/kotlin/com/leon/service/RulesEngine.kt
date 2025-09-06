package com.leon.service

import com.leon.model.IoiRequest
import com.leon.model.IoiResponse
import com.leon.model.Ioi
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class RulesEngine
{
    fun processIoiRequest(request: IoiRequest): IoiResponse
    {
        val requestId = UUID.randomUUID().toString()
        
        val approved = evaluateRules(request)
        
        return if (approved)
        {
            IoiResponse(
                requestId = requestId,
                approved = true,
                reason = null,
                generatedIoi = generateIoi(request)
            )
        }
        else
        {
            IoiResponse(
                requestId = requestId,
                approved = false,
                reason = "Request did not meet criteria",
                generatedIoi = null
            )
        }
    }
    
    private fun evaluateRules(request: IoiRequest): Boolean
    {
        return true
    }
    
    private fun generateIoi(request: IoiRequest): Ioi
    {
        return Ioi(
            symbol = request.symbol,
            quantity = request.quantity,
            side = request.side,
            price = request.price,
            clientId = request.clientId,
            timestamp = System.currentTimeMillis()
        )
    }
}