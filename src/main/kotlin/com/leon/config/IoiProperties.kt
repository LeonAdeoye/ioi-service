package com.leon.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ioi")
class IoiProperties
{
    var pricingServiceUrl: String = "http://localhost:20015"
    var marketServiceUrl: String = "http://localhost:20019"
    var exchangeServiceUrl: String = "http://localhost:20014"
    var domainServiceUrl: String = "http://localhost:20009"
    var configurationServiceUrl: String = "http://localhost:20001"
    var defaultLifetimeMinutes: Long = 15
    var regenerationIntervalSeconds: Long = 60
    var rules: RuleThresholds = RuleThresholds()
}
