package com.example.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductLinkResponse(
    val id: Int,
    val productId: Int,
    val counterpartyId: Int,
    val url: String
)
