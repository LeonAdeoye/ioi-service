package com.leon.model

import java.util.UUID

data class Ioi(
    val ric: String,
    val trader: String,
    val quantity: Long,
    val side: Side,
    val price: Double?,
    val originalOrderType: OrderType,
    val originalMarket: String,
    val clientIds: List<String>,
    val BloombergQualifier: String,
    val timestamp: Long,
    val lifeTimeInMinutes: Long,
    val comment: String,
    val requestId: UUID,
    val ioiFlags: List<String>,
    val fixMessage: String,
    val source: String,
    val status: String = "LIVE"
)
