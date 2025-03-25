package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class CodesResponse(
    val id: Long? = null,
    val code: String,
)
