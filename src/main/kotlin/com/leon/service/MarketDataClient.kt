package com.leon.service

import com.leon.config.IoiProperties
import com.leon.model.LastPriceDto
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class MarketDataClient(builder: RestClient.Builder, properties: IoiProperties)
{
    private val logger = LoggerFactory.getLogger(MarketDataClient::class.java)
    private val restClient = builder.baseUrl(properties.marketServiceUrl).build()

    fun subscribe(ric: String)
    {
        try
        {
            restClient.post()
                .uri("/subscribe")
                .contentType(MediaType.APPLICATION_JSON)
                .body(mapOf("rics" to listOf(ric), "dataSource" to null))
                .retrieve()
                .toBodilessEntity()
            logger.info("Subscribed to last price for {}", ric)
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("Last-price subscribe failed for {}: HTTP {}", ric, e.statusCode.value())
        }
        catch (e: Exception)
        {
            logger.error("Last-price subscribe failed for {}: {}", ric, e.message)
        }
    }

    fun unsubscribe(ric: String)
    {
        try
        {
            restClient.delete()
                .uri("/unsubscribe/{ric}", ric)
                .retrieve()
                .toBodilessEntity()
            logger.info("Unsubscribed from last price for {}", ric)
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("Last-price unsubscribe failed for {}: HTTP {}", ric, e.statusCode.value())
        }
        catch (e: Exception)
        {
            logger.error("Last-price unsubscribe failed for {}: {}", ric, e.message)
        }
    }

    fun getLastPrice(ric: String): Double?
    {
        return try
        {
            val response = restClient.get()
                .uri("/last-price/{ric}", ric)
                .retrieve()
                .body(LastPriceDto::class.java)

            logger.info("Fetched last price {} for {}", response?.price, ric)
            response?.price
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("Last-price lookup failed for {}: HTTP {}", ric, e.statusCode.value())
            null
        }
        catch (e: Exception)
        {
            logger.error("Last-price lookup failed for {}: {}", ric, e.message)
            null
        }
    }
}
