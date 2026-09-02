package com.leon.model

data class IoiResponse(
    val requestId: String,
    val approved: Boolean,
    val reason: String?
)
