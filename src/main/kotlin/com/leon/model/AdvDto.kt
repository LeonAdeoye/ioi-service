package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AdvDto(
    val instrumentCode: String? = null,
    val adv: Long? = null
)
