package com.example.data.dto

import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductResponse(
    val id: Int? = null,
    val name: String,
    val description: String,

    /**
     * Kotlin kotlinx.serialization не поддерживает BigDecimal по умолчанию, поэтому добавлен кастомный сериализатор
     * Это предотвратит проблемы с округлением.
     * BigDecimalSerializer - корректно сериализует/десериализует.
     */
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal, // Используем BigDecimal для работы с деньгами
    val hasSuppliers: Boolean,
    val supplierCount: Int
)
