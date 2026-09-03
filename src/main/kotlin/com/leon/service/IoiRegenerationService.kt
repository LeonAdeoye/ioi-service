package com.leon.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.leon.disruptors.DisruptorPayload
import com.leon.model.IoiRegenerationCommand
import org.springframework.stereotype.Service

@Service
class IoiRegenerationService(
    private val objectMapper: ObjectMapper,
    private val ioiRegenerationDisruptorStarter: IoiRegenerationDisruptorStarter
)
{
    fun enqueue(command: IoiRegenerationCommand)
    {
        val json = objectMapper.writeValueAsString(command)
        ioiRegenerationDisruptorStarter.push(DisruptorPayload(PAYLOAD_TYPE, json))
    }

    companion object
    {
        const val PAYLOAD_TYPE = "IOI_REGENERATION"
    }
}
