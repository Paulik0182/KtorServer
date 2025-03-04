package com.example.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class WarehouseLocationResponse(
    val id: Int,
    val counterpartyId: Int,
    val locationCode: String
)
