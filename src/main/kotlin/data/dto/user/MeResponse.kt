package com.example.data.dto.user

@kotlinx.serialization.Serializable
data class MeResponse(
    val userId: Long,
    val role: String,
    val counterpartyId: Long? = null
)
