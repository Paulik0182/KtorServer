package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementUnitTranslationResponse(
    val id: Long? = null,
    val measurementUnitId: Long,
    val languageCode: String,
    val name: String,
    val abbreviation: String?
)
