package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PriceDto(
    val instrumentCode: String? = null,
    val closePrice: Double? = null,
    val openPrice: Double? = null
)
