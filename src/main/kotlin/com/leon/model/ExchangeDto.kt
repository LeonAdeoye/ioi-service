package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ExchangeDto(
    val exchangeName: String? = null,
    val exchangeAcronym: String? = null,
    val timezone: String? = null,
    val openTime: String? = null,
    val closeTime: String? = null,
    val lunchStart: String? = null,
    val lunchEnd: String? = null,
    val currency: String? = null
)
