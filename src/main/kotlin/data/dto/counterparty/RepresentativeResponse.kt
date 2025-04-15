package com.example.data.dto.counterparty

import kotlinx.serialization.Serializable

@Serializable
data class RepresentativeResponse(
    val id: Long? = null,
    val counterpartyId: Long,
    val fullName: String,
    val position: Int,
    val contacts: List<CounterpartyContactResponse> = emptyList(),
    val contactsIds: List<Long>? = emptyList(),
)
