package com.leon.service

import com.leon.config.IoiProperties
import com.leon.model.FxRateDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class ExchangeServiceClient(builder: RestClient.Builder, properties: IoiProperties)
{
    private val logger = LoggerFactory.getLogger(ExchangeServiceClient::class.java)
    private val restClient = builder.baseUrl(properties.exchangeServiceUrl).build()

    fun getFxRate(currency: String): Double?
    {
        if (currency.equals("USD", ignoreCase = true))
            return 1.0

        return try
        {
            val response = restClient.get()
                .uri("/fx/{currency}", currency.uppercase())
                .retrieve()
                .body(FxRateDto::class.java)

            logger.info("Fetched FX rate {} for {}", response?.rate, currency)
            response?.rate
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("FX lookup failed for {}: HTTP {}", currency, e.statusCode.value())
            null
        }
        catch (e: Exception)
        {
            logger.error("FX lookup failed for {}: {}", currency, e.message)
            null
        }
    }

    fun convertToUsd(amount: Double, currency: String): Double?
    {
        val fxRate = getFxRate(currency) ?: return null
        if (fxRate == 0.0)
            return null
        return amount / fxRate
    }
}
