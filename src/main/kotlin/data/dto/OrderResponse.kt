package com.example.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val id: Int? = null,
    val orderDate: String,
    val counterpartyId: Int,
    val counterpartyName: String,
    val items: List<OrderItemResponse> = emptyList()
)
