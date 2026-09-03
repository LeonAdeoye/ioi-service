package com.leon.model

data class IoiBlockedFailure(
    val requestId: String,
    val trader: String,
    val ric: String,
    val originalMarket: String,
    val reason: String,
    val timestamp: Long,
    val source: String,
    val blockType: String
)
