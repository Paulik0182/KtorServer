package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class CityTranslationResponse(
    val id: Long? = null,
    val cityId: Long,
    val languageCode: String,
    val name: String
)
