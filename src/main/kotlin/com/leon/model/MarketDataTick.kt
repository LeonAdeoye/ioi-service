package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class MarketDataTick(
    val ric: String? = null,
    val price: Double? = null
)
