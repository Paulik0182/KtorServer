package com.example.data.dto.dictionaries

import kotlinx.serialization.Serializable

@Serializable
data class CategoryTranslationResponse(
    val id: Long? = null,
    val categoryId: Long,
    val languageCode: String, // код перевода. Например: ru, pl, en
    val name: String // сам перевод категории
)
