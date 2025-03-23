package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductLinkResponse(
    val id: Long? = null,
    val productId: Long?,
    val counterpartyId: Long?,
    val url: String
)
