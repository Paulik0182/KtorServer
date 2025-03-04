package com.example.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageResponse(
    val id: Int,
    val productId: Int,
    val imageBase64: String // Изображение в base64
)

