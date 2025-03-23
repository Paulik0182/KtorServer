package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class CountryTranslationResponse(
    val id: Long? = null,
    val countryId: Long,
    val languageCode: String,
    val name: String
)
