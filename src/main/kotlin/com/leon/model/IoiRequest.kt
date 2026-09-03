package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class IoiRequest(
    val ric: String,
    val trader: String = "",
    val quantity: Long,
    val side: Side,
    val price: Double?,
    val clientIds: List<String> = emptyList(),
    val BloombergQualifier: String = "",
    val timestamp: Long,
    val lifeTimeInMinutes: Long? = null,
    val comment: String = "",
    val requestId: UUID,
    val ioiFlags: List<String> = emptyList(),
    val originalMarket: String,
    val originalOrderType: OrderType,
    val source: String = "REST",
    val originalQuantity: Long? = null
)
