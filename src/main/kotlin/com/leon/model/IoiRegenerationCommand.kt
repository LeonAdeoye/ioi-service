package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class IoiRegenerationCommand(
    val action: IoiRegenerationAction,
    val request: IoiRequest,
    val originalQuantity: Long,
    val nextQuantity: Long = 0
)
