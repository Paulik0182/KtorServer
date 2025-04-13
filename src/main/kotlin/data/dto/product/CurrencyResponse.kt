package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class CurrencyResponse(
    val id: Long,
    val code: String,
    val symbol: String,
    val name: String
)
