package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductSupplierResponse(
    val id: Long? = null,

    val productId: Long,
    val productName: String,

    val counterpartyId: Long,
    val counterpartyName: String?
)
