package com.leon.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.leon.disruptors.DisruptorEvent
import com.leon.model.IoiRequest
import com.leon.service.RulesEngine
import com.lmax.disruptor.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class IoiProcessingEventHandler(
    private val objectMapper: ObjectMapper,
    private val rulesEngine: RulesEngine
) : EventHandler<DisruptorEvent>
{
    private val logger = LoggerFactory.getLogger(IoiProcessingEventHandler::class.java)

    override fun onEvent(event: DisruptorEvent, sequence: Long, endOfBatch: Boolean)
    {
        val json = event.payload?.payload
        if (json.isNullOrBlank())
            return

        try
        {
            val request = objectMapper.readValue(json, IoiRequest::class.java)
            rulesEngine.processIoiRequest(request)
        }
        catch (e: Exception)
        {
            logger.error("Failed to process IOI disruptor event", e)
        }
    }
}
