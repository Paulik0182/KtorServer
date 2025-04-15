package com.example.data.dto.counterparty

import com.example.data.dto.order.OrderResponse
import com.example.data.dto.product.ProductCounterpartyResponse
import com.example.data.dto.product.ProductLinkResponse
import com.example.data.dto.product.ProductSupplierResponse
import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyResponse(
    val id: Long? = null,
    val shortName: String,

    val companyName: String?,
    val type: String,
    val isSupplierOld: Boolean,
    val productCountOld: Int,

    val isSupplier: Boolean,
    val isWarehouse: Boolean,
    val isCustomer: Boolean,
    val isLegalEntity: Boolean,

    val imagePath: String?,

    val nip: String?,
    val krs: String?,

    val firstName: String?,
    val lastName: String?,

    val counterpartyRepresentatives: List<RepresentativeResponse> = emptyList(),
    val representativesIds: List<Long>? = emptyList(),
    val representativesName: String?,
    val representativesContact: List<String>? = emptyList(), // Должна собираться строчка контакта (пример: Тел. +48 000-000-000, E-mail: pop@hot.pl)

    val counterpartyContacts: List<CounterpartyContactResponse> = emptyList(),
    val contactIds: List<Long>? = emptyList(),
    val counterpartyContact: List<String>? = emptyList(), // Должна собираться строчка контакта (пример: Тел. +48 000-000-000, E-mail: pop@hot.pl)

    val counterpartyBankAccounts: List<BankAccountResponse> = emptyList(),
    val bankAccountIds: List<Long>? = emptyList(),
    val bankAccountInformation: List<String>? = emptyList(), // должна быть строка из названия банка, номера счета и обозначение валюты счета

    val counterpartyAddresses: List<CounterpartyAddressResponse> = emptyList(),
    val addressesIds: List<Long> = emptyList(),
    val addressesInformation: List<String>? = emptyList(), // должна быть строка из Адреса (например: Страна, город, улица, дом, если есть кв. или локация)

    val orders: List<OrderResponse>? = emptyList(),
    val orderIds: List<Long>? = emptyList(),

    val productCounterparties: List<ProductCounterpartyResponse> = emptyList(),

    val productLinks: List<ProductLinkResponse>? = emptyList(),
    val productLinkIds: List<Long>? = emptyList(),

    val productSuppliers: List<ProductSupplierResponse>? = emptyList(),
    val productSupplierIds: List<Long>? = emptyList(),
)
