package com.leon.service

import com.leon.disruptors.DisruptorPayload
import com.leon.disruptors.DisruptorService
import com.leon.handlers.IoiRegenerationEventHandler
import com.leon.handlers.JournalEventHandler
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.stereotype.Service

@Service
class IoiRegenerationDisruptorStarter(
    disruptorServiceProvider: ObjectProvider<DisruptorService>,
    private val journalEventHandler: JournalEventHandler,
    private val ioiRegenerationEventHandler: IoiRegenerationEventHandler
)
{
    private val logger = LoggerFactory.getLogger(IoiRegenerationDisruptorStarter::class.java)
    private val disruptorService = disruptorServiceProvider.getObject()

    @PostConstruct
    fun start()
    {
        disruptorService.start("REGEN", journalEventHandler, ioiRegenerationEventHandler)
        logger.info("Regeneration IOI disruptor started")
    }

    fun push(payload: DisruptorPayload)
    {
        disruptorService.push(payload)
    }

    @PreDestroy
    fun stop()
    {
        disruptorService.stop()
        logger.info("Regeneration IOI disruptor stopped")
    }
}
