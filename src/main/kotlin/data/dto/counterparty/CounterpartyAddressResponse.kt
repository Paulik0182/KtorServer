package com.example.data.dto.counterparty

import com.example.data.dto.dictionaries.CityResponse
import com.example.data.dto.dictionaries.CountryResponse
import kotlinx.serialization.Serializable

@Serializable
data class CounterpartyAddressResponse(
    val id: Long? = null,
    val counterpartyId: Long,
    val counterpartyShortName: List<String>? = emptyList(),
    val counterpartyFirstLastName: List<String>? = emptyList(), // нужна строка из имени и фамилии контрагента, если он есть

    val countryId: Long,
    val country: CountryResponse? = null,
    val countryName: String?,

    val cityId: Long,
    val city: CityResponse? = null,
    val cityName: String?,

    val counterpartyContactId: Long?,

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