package com.leon.service

import com.crankuptheamps.client.Client
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.DependsOn
import org.springframework.stereotype.Service

@Service
@DependsOn("ioiDisruptorStarter")
class AmpsSubscriberService(private val ioiIngestService: IoiIngestService)
{
    private val logger = LoggerFactory.getLogger(AmpsSubscriberService::class.java)

    @Value("\${amps.server.url}")
    private lateinit var serverUrl: String

    @Value("\${amps.inbound.topic.name}")
    private lateinit var topicName: String

    @Value("\${amps.inbound.client.name:IoiSubscriber}")
    private lateinit var clientName: String

    @Value("\${amps.enabled:true}")
    private var ampsEnabled: Boolean = true

    private var ampsClient: Client? = null
    private var subscriberThread: Thread? = null
    @Volatile
    private var running = false

    @PostConstruct
    fun initialize()
    {
        if (!ampsEnabled)
        {
            logger.info("AMPS subscription is disabled via configuration")
            return
        }

        running = true
        subscriberThread = Thread({ subscribeLoop() }, "amps-ioi-inbound")
        subscriberThread?.isDaemon = true
        subscriberThread?.start()
    }

    private fun subscribeLoop()
    {
        try
        {
            logger.info("Initializing AMPS inbound connection to $serverUrl")
            val client = Client(clientName)
            client.connect(serverUrl)
            client.logon()
            ampsClient = client
            logger.info("Subscribing to AMPS inbound topic $topicName")

            for (message in client.subscribe(topicName))
            {
                if (!running)
                    break

                val data = message.data
                if (data.isNullOrBlank())
                    continue

                val accepted = ioiIngestService.enqueueJson(data)
                logger.info("Enqueued {} IOI request(s) from AMPS topic {}", accepted, topicName)
            }
        }
        catch (e: Exception)
        {
            if (running)
                logger.error("AMPS inbound subscription failed", e)
            else
                logger.info("AMPS inbound subscription stopped")
        }
    }

    @PreDestroy
    fun shutdown()
    {
        running = false
        try
        {
            ampsClient?.disconnect()
            logger.info("Disconnected AMPS inbound client")
        }
        catch (e: Exception)
        {
            logger.error("Error disconnecting AMPS inbound client", e)
        }

        subscriberThread?.interrupt()
    }
}
