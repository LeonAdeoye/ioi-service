package com.leon.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ConfigurationDto(
    val id: String? = null,
    val key: String = "",
    val value: String = "",
    val owner: String = "",
    val lastUpdatedBy: String? = null,
    val lastUpdatedOn: String? = null
)
