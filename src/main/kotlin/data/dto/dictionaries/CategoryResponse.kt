package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class CategoryResponse(
    val id: Long? = null,
    val name: String, // локализованное имя (имя по умолчанию)
    val imageUrl: String?,
    val translations: List<CategoryTranslationResponse> = emptyList(), // Перевод названий категорий на другие языки
    val subcategories: List<SubcategoryResponse> = emptyList() // Подкатегории - многоязычность
)
