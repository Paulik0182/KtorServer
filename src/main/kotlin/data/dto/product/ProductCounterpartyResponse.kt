package com.example.data.dto.product

import com.example.data.dto.counterparty.CounterpartyResponse
import com.example.data.dto.dictionaries.MeasurementUnitResponse
import kotlinx.serialization.Serializable

@Serializable
data class ProductCounterpartyResponse(
    val productId: Long,
    val productName: String,
    val counterpartyId: Long,
    val counterpartyName: String?,
//    val counterparty: CounterpartyResponse,
    val stockQuantity: Int, // Количество товара у контрагента (склад)
    val role: String, // Роль (например, "supplier", "warehouse") !Пока это поле не используем, в таблице контрагента есть флаги для обозначения типа контрагента
    val minStockQuantity: Int, // Неснижаемый остаток
    val warehouseLocationCodes: List<String>?, // JSONB список кодов мест хранения товара на складе
    val measurementUnitId: Long, // Еденица измерения. Этот параметр должен братся автоматически из таблицы products, на уровне БД это сделано! сделать на уровне сервера, или сделать проверку.
    val measurementUnitList: MeasurementUnitResponse?,
    val measurementUnit: String?,
    val measurementUnitAbbreviation: String?
)
