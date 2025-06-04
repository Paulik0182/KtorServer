package com.example.data.error.addres

import kotlinx.serialization.Serializable

@Serializable
data class AddressErrorResponse(
    val code: String,
    val message: String
)
