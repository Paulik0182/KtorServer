package com.example.data.error

import kotlinx.serialization.Serializable

@Serializable
data class LoginErrorResponse(
    val code: String,
    val message: String
)
