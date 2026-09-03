package com.leon.service

import com.leon.disruptors.DisruptorPayload
import com.leon.disruptors.DisruptorService
import com.leon.handlers.IoiProcessingEventHandler
import com.leon.handlers.JournalEventHandler
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IoiDisruptorStarter(
    private val disruptorService: DisruptorService,
    private val journalEventHandler: JournalEventHandler,
    private val ioiProcessingEventHandler: IoiProcessingEventHandler
)
{
    private val logger = LoggerFactory.getLogger(IoiDisruptorStarter::class.java)

    @PostConstruct
    fun start()
    {
        disruptorService.start("INBOUND", journalEventHandler, ioiProcessingEventHandler)
        logger.info("Inbound IOI disruptor started")
    }

    fun push(payload: DisruptorPayload)
    {
        disruptorService.push(payload)
    }

    @PreDestroy
    fun stop()
    {
        disruptorService.stop()
        logger.info("Inbound IOI disruptor stopped")
    }
}
