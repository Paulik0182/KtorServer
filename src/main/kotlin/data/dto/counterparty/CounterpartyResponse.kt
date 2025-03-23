package com.example.data.dto.counterparty

import com.example.data.dto.order.OrderResponse
import com.example.data.dto.product.ProductCounterpartyResponse
import com.example.data.dto.product.ProductLinkResponse
import com.example.data.dto.product.ProductSupplierResponse
import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyResponse(
    val id: Long? = null,
    val name: String,
    val type: String,
    val isSupplierOld: Boolean,
    val productCountOld: Int,
    val isSupplier: Boolean,
    val isWarehouse: Boolean,
    val isCustomer: Boolean,
    val counterpartyContacts: List<CounterpartyContactResponse> = emptyList(),
    val counterpartyAddresses: List<CounterpartyAddressResponse> = emptyList(),
    val orders: List<OrderResponse> = emptyList(),
    val productCounterparties: List<ProductCounterpartyResponse> = emptyList(),
    val productLinks: List<ProductLinkResponse> = emptyList(),
    val productSuppliers: List<ProductSupplierResponse> = emptyList()
)
