package com.leon.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ioi")
class IoiProperties
{
    var pricingServiceUrl: String = "http://localhost:20015"
    var exchangeServiceUrl: String = "http://localhost:20014"
    var domainServiceUrl: String = "http://localhost:20009"
    var rules: RuleThresholds = RuleThresholds()
}

class RuleThresholds
{
    var minNotionalUsd: Double = 100_000.0
    var minAdvPercentage: Double = 5.0
    var minQuantity: Long = 50_000L
    var maxPriceDeviationPercent: Double = 2.0
}
