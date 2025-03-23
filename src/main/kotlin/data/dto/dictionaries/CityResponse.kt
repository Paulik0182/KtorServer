package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class CityResponse(
    val id: Long? = null,
    val countryId: Long,
    val name: String,
    val translations: List<CityTranslationResponse> = emptyList()
)
