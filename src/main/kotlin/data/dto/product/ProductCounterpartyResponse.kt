package com.example.data.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductCounterpartyResponse(
    val productId: Long,
    val counterpartyId: Long,
    val stockQuantity: Int, // Количество товара у контрагента (склад)
    val role: String, // Роль (например, "supplier", "warehouse") !Пока это поле не используем, в таблице контрагента есть флаги для обозначения типа контрагента
    val minStockQuantity: Int, // Неснижаемый остаток
    val warehouseLocationCodes: List<String>?, // JSONB список кодов мест хранения товара на складе
    val measurementUnitId: Long // Еденица измерения. Этот параметр должен братся автоматически из таблицы products, на уровне БД это сделано! сделать на уровне сервера, или сделать проверку.
)
