package com.leon.handlers

import com.fasterxml.jackson.databind.ObjectMapper
import com.leon.disruptors.DisruptorEvent
import com.leon.model.IoiRegenerationAction
import com.leon.model.IoiRegenerationCommand
import com.leon.model.IoiRequest
import com.leon.service.AmpsPublisherService
import com.leon.service.FixIoiMessageBuilder
import com.leon.service.IoiBookService
import com.leon.service.IoiIngestService
import com.leon.service.IoiRegenerationService
import com.lmax.disruptor.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class IoiRegenerationEventHandler(
    private val objectMapper: ObjectMapper,
    private val fixIoiMessageBuilder: FixIoiMessageBuilder,
    private val ampsPublisherService: AmpsPublisherService,
    private val ioiIngestService: IoiIngestService,
    private val ioiBookService: IoiBookService
) : EventHandler<DisruptorEvent>
{
    private val logger = LoggerFactory.getLogger(IoiRegenerationEventHandler::class.java)

    override fun onEvent(event: DisruptorEvent, sequence: Long, endOfBatch: Boolean)
    {
        val payload = event.payload ?: return
        if (payload.payloadType.isNotBlank() && payload.payloadType != IoiRegenerationService.PAYLOAD_TYPE)
            return

        val json = payload.payload
        if (json.isBlank())
            return

        try
        {
            val command = objectMapper.readValue(json, IoiRegenerationCommand::class.java)
            when (command.action)
            {
                IoiRegenerationAction.CANCEL_ONLY -> publishCancel(command.request)
                IoiRegenerationAction.CANCEL_AND_RECREATE ->
                {
                    publishCancel(command.request)
                    enqueueRecreate(command)
                }
                IoiRegenerationAction.RECREATE_ONLY -> enqueueRecreate(command)
            }
        }
        catch (e: Exception)
        {
            logger.error("Failed to process IOI regeneration disruptor event", e)
        }
    }

    private fun publishCancel(request: IoiRequest)
    {
        val fixMessage = fixIoiMessageBuilder.buildCancel(request)
        ampsPublisherService.publishFixIoi(request.requestId.toString(), request.ric, fixMessage)
        ioiBookService.markCancelled(request.requestId, false)
        logger.info("Published FIX CANCEL for IOI {}", request.requestId)
    }

    private fun enqueueRecreate(command: IoiRegenerationCommand)
    {
        if (command.nextQuantity <= 0L)
        {
            logger.info("Skipping recreate for IOI {} because remaining quantity is zero", command.request.requestId)
            return
        }

        val recreate = command.request.copy(
            requestId = UUID.randomUUID(),
            quantity = command.nextQuantity,
            originalQuantity = command.originalQuantity,
            timestamp = System.currentTimeMillis()
        )
        ioiIngestService.enqueue(recreate)
        logger.info("Enqueued recreate IOI {} from cancelled IOI {}", recreate.requestId, command.request.requestId)
    }
}
