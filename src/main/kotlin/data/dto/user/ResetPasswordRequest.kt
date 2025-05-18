package com.example.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(val token: String, val newPassword: String)
