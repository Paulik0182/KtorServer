package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductCodeResponse(
    val productId: Long,
    val codId: Long?,
    val codeName: String,
    val code: List<CodesResponse> = emptyList()
)

