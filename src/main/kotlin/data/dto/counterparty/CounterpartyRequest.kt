package com.example.data.dto.counterparty

import com.example.data.dto.product.*
import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyRequest(
    val shortName: String,

    val companyName: String?,
    val type: String,

    val isSupplier: Boolean = false,
    val isWarehouse: Boolean = false,
    val isCustomer: Boolean = false,
    val isLegalEntity: Boolean = false,

    val imagePath: String?,
    val nip: String?,
    val krs: String?,
    val firstName: String?,
    val lastName: String?,


    val representatives: List<RepresentativeRequest>? = emptyList(),
    val contacts: List<CounterpartyContactRequest>? = emptyList(),
    val bankAccounts: List<BankAccountRequest>? = emptyList(),
    val addresses: List<CounterpartyAddressRequest>? = emptyList(),
    val orders: List<OrderRequest>? = emptyList(),
    val productCounterparties: List<ProductCounterpartyRequest> = emptyList(),
    val productLinks: List<ProductLinkRequest>? = emptyList(),
    val productSuppliers: List<ProductSupplierRequest>? = emptyList(),
)

@Serializable
data class RepresentativeRequest(
    val fullName: String?,
    val position: Int = 0,
    val contacts: List<CounterpartyContactRequest> = emptyList()
)

@Serializable
data class CounterpartyContactRequest(
    val contactType: String?,
    val contactValue: String?,
    val countryCodeId: Long? = null,
    val representativeId: Long? = null
)

@Serializable
data class BankAccountRequest(
    val accountNumber: String?,
    val bankName: String,
    val swiftCode: String?,
    val currencyId: Long
)

@Serializable
data class CounterpartyAddressRequest(
    val countryId: Long,
    val postalCode: String?,
    val streetName: String,
    val houseNumber: String,
    val locationNumber: String?,
    val latitude: Double?,
    val longitude: Double?,

    val entranceNumber: String?,
    val floor: String?,
    val numberIntercom: String?,
)

@Serializable
data class OrderRequest(
    val orderIds: Long,
    val orderStatus: Int
)
