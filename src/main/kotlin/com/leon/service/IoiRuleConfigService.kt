package com.leon.service

import com.leon.config.IoiProperties
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

@Service
class IoiRuleConfigService(
    private val configurationServiceClient: ConfigurationServiceClient,
    private val properties: IoiProperties,
    @Lazy private val ioiRegenerationScheduler: IoiRegenerationScheduler
)
{
    private val logger = LoggerFactory.getLogger(IoiRuleConfigService::class.java)

    @PostConstruct
    fun initialize()
    {
        reconfigure()
    }

    fun reconfigure()
    {
        val configurations = configurationServiceClient.getConfigurationsByOwner(SYSTEM_OWNER)
        if (configurations.isEmpty())
            logger.warn("No system configurations retrieved; keeping current IOI rule thresholds and regeneration defaults")
        else
        {
            val valuesByKey = configurations.associate { it.key to it.value }
            applyDouble(valuesByKey, KEY_MIN_NOTIONAL) { properties.rules.minNotionalUsd = it }
            applyDouble(valuesByKey, KEY_MIN_ADV_PERCENTAGE) { properties.rules.minAdvPercentage = it }
            applyLong(valuesByKey, KEY_MIN_QUANTITY) { properties.rules.minQuantity = it }
            applyDouble(valuesByKey, KEY_MAX_PRICE_DEVIATION) { properties.rules.maxPriceDeviationPercent = it }
            applyLong(valuesByKey, KEY_DEFAULT_LIFETIME) { properties.defaultLifetimeMinutes = it }
            applyLong(valuesByKey, KEY_REGENERATION_INTERVAL) { properties.regenerationIntervalSeconds = it.coerceAtLeast(1L) }
        }

        logger.info(
            "IOI runtime config: minNotionalUsd={}, minAdvPercentage={}, minQuantity={}, maxPriceDeviationPercent={}, defaultLifetimeMinutes={}, regenerationIntervalSeconds={}",
            properties.rules.minNotionalUsd,
            properties.rules.minAdvPercentage,
            properties.rules.minQuantity,
            properties.rules.maxPriceDeviationPercent,
            properties.defaultLifetimeMinutes,
            properties.regenerationIntervalSeconds
        )

        ioiRegenerationScheduler.reschedule()
    }

    private fun applyDouble(valuesByKey: Map<String, String>, key: String, setter: (Double) -> Unit)
    {
        val raw = valuesByKey[key]
        if (raw.isNullOrBlank())
        {
            logger.warn("System configuration key {} is missing; keeping current value", key)
            return
        }

        val parsed = raw.toDoubleOrNull()
        if (parsed == null)
        {
            logger.warn("System configuration key {} has invalid value {}; keeping current value", key, raw)
            return
        }

        setter(parsed)
        logger.info("Applied system configuration {}={}", key, parsed)
    }

    private fun applyLong(valuesByKey: Map<String, String>, key: String, setter: (Long) -> Unit)
    {
        val raw = valuesByKey[key]
        if (raw.isNullOrBlank())
        {
            logger.warn("System configuration key {} is missing; keeping current value", key)
            return
        }

        val parsed = raw.toLongOrNull() ?: raw.toDoubleOrNull()?.toLong()
        if (parsed == null)
        {
            logger.warn("System configuration key {} has invalid value {}; keeping current value", key, raw)
            return
        }

        setter(parsed)
        logger.info("Applied system configuration {}={}", key, parsed)
    }

    companion object
    {
        private const val SYSTEM_OWNER = "system"
        private const val KEY_MIN_NOTIONAL = "ioi.rules.min-notional-usd"
        private const val KEY_MIN_ADV_PERCENTAGE = "ioi.rules.min-adv-percentage"
        private const val KEY_MIN_QUANTITY = "ioi.rules.min-quantity"
        private const val KEY_MAX_PRICE_DEVIATION = "ioi.rules.max-price-deviation-percent"
        private const val KEY_DEFAULT_LIFETIME = "ioi.default-lifetime-minutes"
        private const val KEY_REGENERATION_INTERVAL = "ioi.regeneration-interval-seconds"
    }
}
