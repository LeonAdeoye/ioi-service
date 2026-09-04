package com.leon.service

import com.crankuptheamps.client.Client
import com.fasterxml.jackson.databind.ObjectMapper
import com.leon.model.MarketDataTick
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AmpsMarketDataSubscriberService(
    private val objectMapper: ObjectMapper,
    private val lastPriceService: LastPriceService
)
{
    private val logger = LoggerFactory.getLogger(AmpsMarketDataSubscriberService::class.java)

    @Value("\${amps.server.url}")
    private lateinit var serverUrl: String

    @Value("\${amps.market-data.topic.name:market.data}")
    private lateinit var topicName: String

    @Value("\${amps.market-data.client.name:IoiLastPriceSubscriber}")
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
            logger.info("AMPS last-price subscription is disabled via configuration")
            return
        }

        running = true
        subscriberThread = Thread({ subscribeLoop() }, "amps-ioi-last-price")
        subscriberThread?.isDaemon = true
        subscriberThread?.start()
    }

    private fun subscribeLoop()
    {
        try
        {
            logger.info("Initializing AMPS last-price connection to $serverUrl")
            val client = Client(clientName)
            client.connect(serverUrl)
            client.logon()
            ampsClient = client
            logger.info("Subscribing to AMPS last-price topic $topicName")

            for (message in client.subscribe(topicName))
            {
                if (!running)
                    break

                val data = message.data
                if (data.isNullOrBlank())
                    continue

                try
                {
                    val tick = objectMapper.readValue(data, MarketDataTick::class.java)
                    val ric = tick.ric
                    val price = tick.price
                    if (ric.isNullOrBlank() || price == null)
                        continue

                    lastPriceService.updateTick(ric, price)
                }
                catch (e: Exception)
                {
                    logger.warn("Failed to parse market data tick: {}", e.message)
                }
            }
        }
        catch (e: Exception)
        {
            if (running)
                logger.error("AMPS last-price subscription failed", e)
            else
                logger.info("AMPS last-price subscription stopped")
        }
    }

    @PreDestroy
    fun shutdown()
    {
        running = false
        try
        {
            ampsClient?.disconnect()
            logger.info("Disconnected AMPS last-price client")
        }
        catch (e: Exception)
        {
            logger.error("Error disconnecting AMPS last-price client", e)
        }

        subscriberThread?.interrupt()
    }
}
