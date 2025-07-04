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
data class CounterpartyPatchRequest(
    val shortName: String,

    val companyName: String?,
    val type: String,

    val isSupplier: Boolean = false,
    val isWarehouse: Boolean = false,
    val isCustomer: Boolean = false,
    val isLegalEntity: Boolean = false,

    val nip: String?,
    val krs: String?,
    val firstName: String?,
    val lastName: String?
)

@Serializable
data class CounterpartyImageRequest(
    val imagePath: String?
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
    val id: Long? = null,
    val countryId: Long,
    val cityId: Long,
    val postalCode: String? = null,
    val streetName: String,
    val houseNumber: String,
    val locationNumber: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,

    val entranceNumber: String? = null,
    val floor: String? = null,
    val numberIntercom: String? = null,

    val isMain: Boolean = false,
    val fullName: String? = null
)

@Serializable
data class OrderRequest(
    val orderIds: Long,
    val orderStatus: Int
)
