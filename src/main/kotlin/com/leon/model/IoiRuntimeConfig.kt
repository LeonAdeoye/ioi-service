package com.leon.model

import com.leon.config.RuleThresholds

data class IoiRuntimeConfig(
    val rules: RuleThresholds,
    val defaultLifetimeMinutes: Long,
    val regenerationIntervalSeconds: Long
)
