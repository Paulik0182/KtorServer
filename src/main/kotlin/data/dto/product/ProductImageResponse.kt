package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageResponse(
    val id: Long? = null,
    val productId: Long,
    val imageUrl: String, // путь к файлу (локальный или полный URL)
    val position: Int?
)

