package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductCategoryResponse(
    val productId: Long,
    val categoryId: Long
)
