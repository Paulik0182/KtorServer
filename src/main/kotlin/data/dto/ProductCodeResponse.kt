package com.example.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductCodeResponse(
    val productId: Int,
    val code: String
)

