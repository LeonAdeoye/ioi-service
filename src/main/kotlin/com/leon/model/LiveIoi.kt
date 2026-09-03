package com.leon.model

data class LiveIoi(
    val request: IoiRequest,
    val originalQuantity: Long,
    val expiresAtMillis: Long
)
