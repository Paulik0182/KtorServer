package com.example.data.dto.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemResponse(
    val id: Long? = null,
    val orderId: Long,
    val productId: Long,
    val quantity: Int,
    val productName: String,
    val measurementUnitId: Long
)
