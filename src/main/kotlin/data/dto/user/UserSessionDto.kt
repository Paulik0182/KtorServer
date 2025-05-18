package com.example.data.dto.user

@kotlinx.serialization.Serializable
data class UserSessionDto(
    val userId: Long,
    val token: String,
    val createdAt: String,
    val expiresAt: String,
    val deviceInfo: String?
)
