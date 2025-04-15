package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class CountryResponse(
    val id: Long? = null,
    val name: String,
    val phoneCode: String,
    val isoCode: String,
    val translations: List<CountryTranslationResponse> = emptyList(),
    val city: List<CityResponse>? = emptyList(),
    val cityIds: List<Long>? = emptyList(),
)
