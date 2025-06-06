package com.example.data.dto.product

import com.example.data.dto.dictionaries.CategoryResponse
import com.example.data.dto.dictionaries.MeasurementUnitResponse
import com.example.data.dto.dictionaries.SubcategoryResponse
import com.example.data.dto.order.OrderItemResponse
import com.example.utils.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductResponse(
    val id: Long? = null,
    val name: String,
    val description: String,
    /**
     * Kotlin kotlinx.serialization не поддерживает BigDecimal по умолчанию, поэтому добавлен кастомный сериализатор
     * Это предотвратит проблемы с округлением.
     * BigDecimalSerializer - корректно сериализует/десериализует.
     */
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal, // Используем BigDecimal для работы с деньгами
    val hasSuppliers: Boolean, // Признак наличия поставщика

    val supplierCount: Int, // Количество поставщиков
    val totalStockQuantity: Int, // количество товара на всех складах
    val minStockQuantity: Int, // неснижаемый остаток

    val isDemanded: Boolean, // флаг востребованности
    val measurementUnitId: Long, // ID единицы измерения
    val measurementUnitList: MeasurementUnitResponse?, // Связь с единицей измерения
    val measurementUnit: String?,
    val measurementUnitAbbreviation: String?,

    val productCodes: List<ProductCodeResponse> = emptyList(), // список кодов товара (штрих-коды, QR-коды).
    val productLinks: List<LinkResponse> = emptyList(), // список интернет-ссылок.
    val productImages: List<ProductImageResponse> = emptyList(), // для картинок
    val productCounterparties: List<ProductCounterpartyResponse> = emptyList(), // Информация о складах, контрагентах
    val productSuppliers: List<ProductSupplierResponse> = emptyList(), // Информация о Поставщике, контрагентах
    val measurementUnits: List<MeasurementUnitResponse> = emptyList(), // Еденицы измерения
    val productOrderItem: List<OrderItemResponse> = emptyList(), // Информация о Заказах товара
    val categories: List<CategoryResponse> = emptyList(), // Категории - многоязычность
    val subcategoryIds: List<Long>? = emptyList(), // список подкатегорий (для фильтра)
    val categoryIds: List<Long> = emptyList(),
    val subcategories: List<SubcategoryResponse> = emptyList(),

    val currencyCode: String?,
    val currencySymbol: String?,
    val currencyName: String?,
    val currencyId: Long?,
)
