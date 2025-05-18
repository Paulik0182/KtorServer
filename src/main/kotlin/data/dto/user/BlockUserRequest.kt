package com.example.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class BlockUserRequest(
    val userId: Long,
    val comment: String? = null
)
