package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class LastPriceDto(
    val ric: String? = null,
    val price: Double? = null,
    val success: Boolean? = null
)
