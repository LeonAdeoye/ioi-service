package com.leon.service

import com.leon.config.IoiProperties
import com.leon.model.ConfigurationDto
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.RestClientResponseException

@Service
class ConfigurationServiceClient(builder: RestClient.Builder, properties: IoiProperties)
{
    private val logger = LoggerFactory.getLogger(ConfigurationServiceClient::class.java)
    private val restClient = builder.baseUrl(properties.configurationServiceUrl).build()

    fun getConfigurationsByOwner(owner: String): List<ConfigurationDto>
    {
        return try
        {
            val response = restClient.get()
                .uri("/configurationByOwner?owner={owner}", owner)
                .retrieve()
                .body(Array<ConfigurationDto>::class.java)

            val configurations = response?.toList() ?: emptyList()
            logger.info("Fetched {} configurations for owner {}", configurations.size, owner)
            configurations
        }
        catch (e: RestClientResponseException)
        {
            logger.warn("Configuration lookup failed for owner {}: HTTP {}", owner, e.statusCode.value())
            emptyList()
        }
        catch (e: Exception)
        {
            logger.error("Configuration lookup failed for owner {}: {}", owner, e.message)
            emptyList()
        }
    }
}
