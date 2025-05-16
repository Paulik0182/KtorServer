package com.example.data.dto.user

import com.example.routing.UserRole
import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val role: UserRole = UserRole.CUSTOMER
)
