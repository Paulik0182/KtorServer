package com.example.data.dto.category

import kotlinx.serialization.Serializable

@Serializable
data class CategoryRequest(
    val name: String,
    val imageBase64: String? = null,
    val translations: List<CategoryTranslationRequest> = emptyList(),
    val subcategories: List<SubcategoryRequest> = emptyList()
)

@Serializable
data class CategoryTranslationRequest(
    val languageCode: String,
    val name: String
)

@Serializable
data class SubcategoryRequest(
    val id: Long? = null, // для PUT
    val name: String,
    val imageBase64: String? = null,
    val translations: List<SubcategoryTranslationRequest> = emptyList()
)

@Serializable
data class SubcategoryTranslationRequest(
    val id: Long? = null, // для PUT
    val languageCode: String,
    val name: String
)
