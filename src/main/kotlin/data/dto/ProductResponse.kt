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
    val supplierCount: Int,
    val stockQuantity: Int, // количество товара на складе
    val minStockQuantity: Int, // неснижаемый остаток
    val productCodes: List<String>, // список кодов товара (штрих-коды, QR-коды).
    val isDemanded: Boolean, // флаг востребованности
    val productLinks: List<ProductLinkResponse>, // список интернет-ссылок.
    val locations: List<WarehouseLocationResponse>, // код места хранения товара
    val images: List<ProductImageResponse> // для картинок
)
