package com.example.data.dto.product

import com.example.utils.BigDecimalSerializer
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class ProductCreateRequest(
    val name: String,
    val description: String,
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
    val hasSuppliers: Boolean,
    val supplierCount: Int,
    val totalStockQuantity: Int,
    val minStockQuantity: Int,
    val isDemanded: Boolean,
    val measurementUnitId: Long,

    val productCodes: List<ProductCodeRequest> = emptyList(),
    val productLinks: List<ProductLinkRequest> = emptyList(),
    val productImages: List<ProductImageRequest> = emptyList(),
    val productCounterparties: List<ProductCounterpartyRequest> = emptyList(),
    val productSuppliers: List<ProductSupplierRequest> = emptyList(),
    val categories: List<Long> = emptyList() // только ID категорий
)

@Serializable
data class ProductCodeRequest(val code: String)

@Serializable
data class ProductLinkRequest(val counterpartyId: Long?, val url: String)

@Serializable
data class ProductImageRequest(val imageBase64: String)

@Serializable
data class ProductCounterpartyRequest(
    val counterpartyId: Long,
    val stockQuantity: Int,
    val role: String,
    val minStockQuantity: Int,
    val warehouseLocationCodes: List<String>?,
    val measurementUnitId: Long
)

@Serializable
data class ProductSupplierRequest(val supplierId: Long)
