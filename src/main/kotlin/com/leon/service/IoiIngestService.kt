package com.leon.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.leon.disruptors.DisruptorPayload
import com.leon.model.IoiRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IoiIngestService(
    private val objectMapper: ObjectMapper,
    private val ioiDisruptorStarter: IoiDisruptorStarter
)
{
    private val logger = LoggerFactory.getLogger(IoiIngestService::class.java)

    fun enqueue(request: IoiRequest)
    {
        val json = objectMapper.writeValueAsString(request)
        ioiDisruptorStarter.push(DisruptorPayload(PAYLOAD_TYPE, json))
    }

    fun enqueueAll(requests: List<IoiRequest>, source: String): Int
    {
        requests.forEach { enqueue(it.copy(source = source)) }
        return requests.size
    }

    fun enqueueJson(json: String, source: String): Int
    {
        return try
        {
            val node = objectMapper.readTree(json)
            when
            {
                node.isArray ->
                {
                    var count = 0
                    node.forEach { element ->
                        if (enqueueNode(element, source))
                            count++
                    }
                    count
                }
                node.isObject -> if (enqueueNode(node, source)) 1 else 0
                else ->
                {
                    logger.warn("Unsupported IOI JSON payload shape")
                    0
                }
            }
        }
        catch (e: Exception)
        {
            logger.error("Failed to parse IOI JSON payload", e)
            0
        }
    }

    private fun enqueueNode(node: JsonNode, source: String): Boolean
    {
        return try
        {
            val request = objectMapper.treeToValue(node, IoiRequest::class.java)
            enqueue(request.copy(source = source))
            true
        }
        catch (e: Exception)
        {
            logger.error("Failed to deserialize IOI request from payload", e)
            false
        }
    }

    companion object
    {
        const val PAYLOAD_TYPE = "IOI_REQUEST"
    }
}
