package com.leon.model

data class IoiResponse(
    val requestId: String,
    val approved: Boolean,
    val reason: String?,
    val generatedIoi: Ioi?
)

data class Ioi(
    val symbol: String,
    val quantity: Long,
    val side: String,
    val price: Double?,
    val clientId: String,
    val timestamp: Long
)