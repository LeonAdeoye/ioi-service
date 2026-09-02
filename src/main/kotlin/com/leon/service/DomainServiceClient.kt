package com.leon.service

import com.leon.config.IoiProperties
import com.leon.model.ExchangeDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class DomainServiceClient(builder: RestClient.Builder, properties: IoiProperties)
{
    private val logger = LoggerFactory.getLogger(DomainServiceClient::class.java)
    private val restClient = builder.baseUrl(properties.domainServiceUrl).build()

    fun getExchangeByAcronym(acronym: String): ExchangeDto?
    {
        return try
        {
            val response = restClient.get()
                .uri("/exchange/acronym/{acronym}", acronym)
                .retrieve()
                .body(ExchangeDto::class.java)

            logger.info("Fetched exchange {} for acronym {}", response?.exchangeName, acronym)
            response
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("Exchange lookup failed for {}: HTTP {}", acronym, e.statusCode.value())
            null
        }
        catch (e: Exception)
        {
            logger.error("Exchange lookup failed for {}: {}", acronym, e.message)
            null
        }
    }
}
