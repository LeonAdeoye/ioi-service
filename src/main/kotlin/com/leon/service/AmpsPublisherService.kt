package com.leon.service

import com.crankuptheamps.client.Client
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AmpsPublisherService(private val objectMapper: ObjectMapper)
{
    private val logger = LoggerFactory.getLogger(AmpsPublisherService::class.java)

    @Value("\${amps.server.url}")
    private lateinit var serverUrl: String

    @Value("\${amps.topic.name}")
    private lateinit var topicName: String

    @Value("\${amps.client.name:IoiPublisher}")
    private lateinit var clientName: String

    @Value("\${amps.enabled:true}")
    private var ampsEnabled: Boolean = true

    private var isConnected = false
    private var ampsClient: Client? = null

    @PostConstruct
    fun initialize()
    {
        if (!ampsEnabled)
        {
            logger.info("AMPS publishing is disabled via configuration")
            return
        }

        try
        {
            logger.info("Initializing AMPS connection to $serverUrl")
            ampsClient = Client(clientName)
            ampsClient?.connect(serverUrl)
            ampsClient?.logon()
            isConnected = true
            logger.info("Successfully connected to AMPS server at $serverUrl")
        }
        catch (e: Exception)
        {
            logger.warn("Failed to connect to AMPS server at $serverUrl. AMPS publishing will be disabled. Error: ${e.message}")
            isConnected = false
            ampsClient = null
        }
    }

    fun publishFixIoi(requestId: String, ric: String, fixMessage: String)
    {
        val payload = mapOf(
            "requestId" to requestId,
            "ric" to ric,
            "fixMessage" to fixMessage
        )
        publish(payload, requestId)
    }

    private fun publish(data: Any, id: String)
    {
        if (!ampsEnabled)
        {
            logger.debug("AMPS publishing is disabled, skipping publish for $id")
            return
        }

        if (!isConnected || ampsClient == null)
        {
            logger.debug("AMPS not connected, attempting to reconnect")
            try
            {
                initialize()
            }
            catch (e: Exception)
            {
                logger.debug("Failed to reconnect to AMPS, skipping publish for $id")
                return
            }
        }

        try
        {
            val jsonPayload = objectMapper.writeValueAsString(data)
            ampsClient?.publish(topicName, jsonPayload)
            logger.info("Published FIX IOI for $id to topic $topicName")
        }
        catch (e: Exception)
        {
            logger.error("Failed to publish FIX IOI for $id", e)
            isConnected = false
            ampsClient = null
        }
    }

    @PreDestroy
    fun shutdown()
    {
        try
        {
            if (ampsClient != null)
            {
                ampsClient?.disconnect()
                logger.info("Disconnected from AMPS server")
            }
        }
        catch (e: Exception)
        {
            logger.error("Error disconnecting from AMPS server", e)
        }
    }
}
