package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class UrlsResponse(
    val id: Long? = null,
    val url: String,
)