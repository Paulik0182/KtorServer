package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class LinkResponse(
    val id: Long? = null,
    val productId: Long?,
    val counterpartyId: Long?,
    val urlId: Long?,
    val urlName: String?,
    val url: List<UrlsResponse> = emptyList()
)
