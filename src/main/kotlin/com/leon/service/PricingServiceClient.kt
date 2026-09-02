package com.leon.service

import com.leon.config.IoiProperties
import com.leon.model.AdvDto
import com.leon.model.PriceDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class PricingServiceClient(builder: RestClient.Builder, properties: IoiProperties)
{
    private val logger = LoggerFactory.getLogger(PricingServiceClient::class.java)
    private val restClient = builder.baseUrl(properties.pricingServiceUrl).build()

    fun getAdv(instrumentCode: String): Long?
    {
        return try
        {
            val response = restClient.get()
                .uri("/adv/{instrumentCode}", instrumentCode)
                .retrieve()
                .body(AdvDto::class.java)

            logger.info("Fetched ADV {} for instrument {}", response?.adv, instrumentCode)
            response?.adv
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("ADV lookup failed for {}: HTTP {}", instrumentCode, e.statusCode.value())
            null
        }
        catch (e: Exception)
        {
            logger.error("ADV lookup failed for {}: {}", instrumentCode, e.message)
            null
        }
    }

    fun getClosePrice(instrumentCode: String): Double?
    {
        return try
        {
            val response = restClient.get()
                .uri("/price/{instrumentCode}", instrumentCode)
                .retrieve()
                .body(PriceDto::class.java)

            logger.info("Fetched close price {} for instrument {}", response?.closePrice, instrumentCode)
            response?.closePrice
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("Price lookup failed for {}: HTTP {}", instrumentCode, e.statusCode.value())
            null
        }
        catch (e: Exception)
        {
            logger.error("Price lookup failed for {}: {}", instrumentCode, e.message)
            null
        }
    }
}
