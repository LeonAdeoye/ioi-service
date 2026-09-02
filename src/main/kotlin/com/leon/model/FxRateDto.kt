package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class FxRateDto(
    val currency: String? = null,
    val rate: Double? = null
)
