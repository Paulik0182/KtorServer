package com.example.data.dto.order

import com.example.utils.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class OrderResponse(
    val id: Long? = null,
    val counterpartyId: Long,
    val counterpartyName: String?,
    val orderStatus: Int,

//    @Serializable(with = LocalDateTimeSerializer::class)
//    val createdAt: LocalDateTime,

    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,

    @Serializable(with = InstantSerializer::class)
    val updatedAt: Instant? = null,

    @Serializable(with = InstantSerializer::class)
    val acceptedAt: Instant? = null,

    @Serializable(with = InstantSerializer::class)
    val completedAt: Instant? = null,

    val items: List<OrderItemResponse> = emptyList()
)
