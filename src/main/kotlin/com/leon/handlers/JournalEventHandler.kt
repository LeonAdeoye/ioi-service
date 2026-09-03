package com.leon.handlers

import com.leon.disruptors.DisruptorEvent
import com.lmax.disruptor.EventHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class JournalEventHandler : EventHandler<DisruptorEvent>
{
    private val logger = LoggerFactory.getLogger(JournalEventHandler::class.java)

    override fun onEvent(event: DisruptorEvent, sequence: Long, endOfBatch: Boolean)
    {
        logger.debug(event.payload?.toString())
    }
}
