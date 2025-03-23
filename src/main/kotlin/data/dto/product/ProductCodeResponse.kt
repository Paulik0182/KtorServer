package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductCodeResponse(
    val productId: Long,
    val code: String
)

