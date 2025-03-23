package com.example.data.dto.order

import com.example.data.dto.dictionaries.MeasurementUnitResponse
import kotlinx.serialization.Serializable

@Serializable
data class OrderItemResponse(
    val id: Long? = null,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val measurementUnitId: Long,
    val measurementUnitList: MeasurementUnitResponse?,
    val measurementUnit: String?,
    val measurementUnitAbbreviation: String?,
)
