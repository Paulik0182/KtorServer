package com.example.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyResponse(
    val id: Int,
    val name: String,
    val type: String,
    val isSupplier: Boolean,
    val productCount: Int
)
