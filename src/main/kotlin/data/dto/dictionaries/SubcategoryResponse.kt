package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class SubcategoryResponse(
    val id: Long? = null,
    val name: String, // локализованное имя (имя по умолчанию)
    val imageUrl: String?,
    val categoryId: Long,
    val translations: List<SubcategoryTranslationResponse> = emptyList()
)
