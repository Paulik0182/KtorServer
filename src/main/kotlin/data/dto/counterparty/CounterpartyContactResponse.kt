package com.example.data.dto.counterparty

import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyContactResponse(
    val id: Long? = null,
    val counterpartyId: Long,
    val contactType: String,
    val contactValue: String,
    val countryCodeId: Long
)
