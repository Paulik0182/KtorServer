package com.example.data.dto.user

import kotlinx.serialization.Serializable

@Serializable
data class ResetRequest(val email: String)
