package com.leon.model

data class IoiFailure(
    val requestId: String,
    val trader: String,
    val ric: String,
    val originalMarket: String,
    val reason: String,
    val timestamp: Long,
    val source: String,
    val quantity: Long,
    val price: Double?,
    val lastPrice: Double? = null,
    val advPercentage: Double? = null
)
