package com.example.data.dto.counterparty

import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyAddressResponse(
    val id: Long? = null,
    val counterpartyId: Long,
    val countryId: Long,
    val postalCode: String?,
    val streetName: String?,
    val houseNumber: String?,
    val locationNumber: String?,
    val latitude: Double?,
    val longitude: Double?,
    val contactId: Long
)
