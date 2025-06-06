package com.example.data.dto.counterparty

import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyContactResponse(
    val id: Long? = null,
    val counterpartyId: Long?,
    val counterpartyName: String?,
    val contactType: String,
    val contactValue: String,
    val countryCodeId: Long?,
    val countryName: String? = null,
    val countryPhoneCode: String? = null,
    val countryIsoCode: String? = null,
    val representativeId: Long?,
    val representativeName: String?,
)
