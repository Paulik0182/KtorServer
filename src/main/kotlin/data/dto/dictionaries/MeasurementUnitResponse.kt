package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class MeasurementUnitResponse(
    val id: Long? = null,
    val name: String,
    val abbreviation: String,
    val translations: List<MeasurementUnitTranslationResponse> = emptyList()
)
