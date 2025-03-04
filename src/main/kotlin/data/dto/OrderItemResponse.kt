package com.example.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemResponse(
    val id: Int? = null,
    val orderId: Int,
    val productId: Int,
    val productName: String,
    val supplierId: Int,
    val quantity: Int
)
