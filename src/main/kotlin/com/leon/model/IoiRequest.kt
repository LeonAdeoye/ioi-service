package com.leon.model

data class IoiRequest(
    val symbol: String,
    val quantity: Long,
    val side: String,
    val price: Double?,
    val clientId: String
)