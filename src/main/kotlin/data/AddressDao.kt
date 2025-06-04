package com.example.data

import com.example.*
import com.example.data.CounterpartyDao.getCounterpartyFullName
import com.example.data.ProductDao.getCounterpartyName
import com.example.data.dto.counterparty.CounterpartyAddressRequest
import com.example.data.dto.counterparty.CounterpartyAddressResponse
import com.example.data.dto.dictionaries.CityResponse
import com.example.data.dto.dictionaries.CityTranslationResponse
import com.example.data.dto.dictionaries.CountryResponse
import com.example.data.dto.dictionaries.CountryTranslationResponse
import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object AddressDao {


    fun getCounterpartyAddresses(counterpartyId: Long, languageCode: String = "ru"): List<CounterpartyAddressResponse> =
        transaction {
            val addresses = CounterpartyAddresses
                .innerJoin(Countries)
                .selectAll().where { CounterpartyAddresses.counterpartyId eq counterpartyId }
                .map {
                    println("🧪 Строка: ${it[CounterpartyAddresses.id]}, contact_id: ${it[CounterpartyAddresses.counterpartyContactId]}")

                    val addressId = it[CounterpartyAddresses.id]
                    val countryId = it[CounterpartyAddresses.countryId]
                    val cityId = it[CounterpartyAddresses.cityId]

                    CounterpartyAddressResponse(
                        id = addressId,
                        counterpartyId = counterpartyId,
                        countryId = countryId,
                        country = getCountry(countryId, languageCode),
                        countryName = getCountryName(countryId),
                        cityId = cityId,
                        city = getCity(cityId, languageCode),
                        cityName = getCityName(cityId),
                        counterpartyContactId = it[CounterpartyAddresses.counterpartyContactId],
                        streetName = it[CounterpartyAddresses.streetName] ?: "",
                        houseNumber = it[CounterpartyAddresses.houseNumber] ?: "",
                        locationNumber = it[CounterpartyAddresses.locationNumber],
                        postalCode = it[CounterpartyAddresses.postalCode],
                        latitude = it[CounterpartyAddresses.latitude]?.toDouble(),
                        longitude = it[CounterpartyAddresses.longitude]?.toDouble(),
                        entranceNumber = it[CounterpartyAddresses.entranceNumber],
                        floor = it[CounterpartyAddresses.floor],
                        numberIntercom = it[CounterpartyAddresses.numberIntercom],
                        counterpartyShortName = getCounterpartyName(counterpartyId)?.let { listOf(it) },
                        counterpartyFirstLastName = listOf(getCounterpartyFullName(counterpartyId)),
                        isMain = it[CounterpartyAddresses.isMain],
                        fullName = it[CounterpartyAddresses.fullName],
                    )
                }
            addresses
        }

    fun getAddressById(addressId: Long, languageCode: String = "ru"): CounterpartyAddressResponse? = transaction {
        val row = CounterpartyAddresses
            .selectAll()
            .where { CounterpartyAddresses.id eq addressId }
            .firstOrNull() ?: return@transaction null

        val countryId = row[CounterpartyAddresses.countryId]
        val cityId = row[CounterpartyAddresses.cityId]
        val counterpartyId = row[CounterpartyAddresses.counterpartyId]

        CounterpartyAddressResponse(
            id = addressId,
            counterpartyId = counterpartyId,
            countryId = countryId,
            country = getCountry(countryId, languageCode),
            countryName = getCountryName(countryId),
            cityId = cityId,
            city = getCity(cityId, languageCode),
            cityName = getCityName(cityId),
            counterpartyContactId = row[CounterpartyAddresses.counterpartyContactId],
            postalCode = row[CounterpartyAddresses.postalCode],
            streetName = row[CounterpartyAddresses.streetName],
            houseNumber = row[CounterpartyAddresses.houseNumber],
            locationNumber = row[CounterpartyAddresses.locationNumber],
            latitude = row[CounterpartyAddresses.latitude]?.toDouble(),
            longitude = row[CounterpartyAddresses.longitude]?.toDouble(),
            entranceNumber = row[CounterpartyAddresses.entranceNumber],
            floor = row[CounterpartyAddresses.floor],
            numberIntercom = row[CounterpartyAddresses.numberIntercom],
            isMain = row[CounterpartyAddresses.isMain],
            fullName = row[CounterpartyAddresses.fullName],
            counterpartyShortName = getCounterpartyName(counterpartyId)?.let { listOf(it) },
            counterpartyFirstLastName = listOf(getCounterpartyFullName(counterpartyId))
        )
    }

    fun addAddress(counterpartyId: Long, address: CounterpartyAddressRequest): Long = transaction {
        validateCityBelongsToCountry(address.cityId, address.countryId)

        CounterpartyAddresses.insert {
            it[this.counterpartyId] = counterpartyId
            it[countryId] = address.countryId
            it[cityId] = address.cityId
            it[postalCode] = address.postalCode
            it[streetName] = address.streetName
            it[houseNumber] = address.houseNumber
            it[locationNumber] = address.locationNumber
            it[latitude] = address.latitude?.toBigDecimal()
            it[longitude] = address.longitude?.toBigDecimal()
            it[entranceNumber] = address.entranceNumber
            it[floor] = address.floor
            it[numberIntercom] = address.numberIntercom
            it[isMain] = address.isMain
            it[fullName] = address.fullName
        } get CounterpartyAddresses.id
    }

    fun deleteAddress(counterpartyId: Long, addressId: Long): Boolean = transaction {
        val deleted = CounterpartyAddresses.deleteWhere {
            (CounterpartyAddresses.id eq addressId) and (CounterpartyAddresses.counterpartyId eq counterpartyId)
        }
        deleted > 0
    }

    fun updateAddresses(counterpartyId: Long, addresses: List<CounterpartyAddressRequest>) = transaction {
        // Удаляем все старые адреса контрагента
        CounterpartyAddresses.deleteWhere { CounterpartyAddresses.counterpartyId eq counterpartyId }

        // Вставляем новые адреса
        addresses.forEach { address ->
            // Валидация: проверяем, что город принадлежит стране
            validateCityBelongsToCountry(address.cityId, address.countryId)

            CounterpartyAddresses.insert {
                it[this.counterpartyId] = counterpartyId
                it[countryId] = address.countryId
                it[cityId] = address.cityId
                it[postalCode] = address.postalCode
                it[streetName] = address.streetName
                it[houseNumber] = address.houseNumber
                it[locationNumber] = address.locationNumber
                it[latitude] = address.latitude?.toBigDecimal()
                it[longitude] = address.longitude?.toBigDecimal()
                it[entranceNumber] = address.entranceNumber
                it[floor] = address.floor
                it[numberIntercom] = address.numberIntercom
            }
        }
    }

    fun patchAddress(counterpartyId: Long, addressId: Long, patch: Map<String, Any?>) = transaction {
        CounterpartyAddresses.update(
            where = { (CounterpartyAddresses.id eq addressId) and (CounterpartyAddresses.counterpartyId eq counterpartyId) }
        ) {
            patch["countryId"]?.let { value -> it[countryId] = value as Long }
            patch["cityId"]?.let { value -> it[cityId] = value as Long }
            patch["postalCode"]?.let { value -> it[postalCode] = value as String }
            patch["streetName"]?.let { value -> it[streetName] = value as String }
            patch["houseNumber"]?.let { value -> it[houseNumber] = value as String }
            patch["locationNumber"]?.let { value -> it[locationNumber] = value as String }
            patch["latitude"]?.let { value -> it[latitude] = (value as Double).toBigDecimal() }
            patch["longitude"]?.let { value -> it[longitude] = (value as Double).toBigDecimal() }
            patch["entranceNumber"]?.let { value -> it[entranceNumber] = value as String }
            patch["floor"]?.let { value -> it[floor] = value as String }
            patch["numberIntercom"]?.let { value -> it[numberIntercom] = value as String }
            patch["isMain"]?.let { value -> it[isMain] = value as Boolean }
            patch["fullName"]?.let { value -> it[fullName] = value as String }
        }
    }

    fun validateCityBelongsToCountry(cityId: Long, countryId: Long) {
        val city = Cities.selectAll().where { Cities.id eq cityId }.singleOrNull()
            ?: error(HttpStatusCode.BadRequest, "City not found")

        if (city[Cities.countryId] != countryId) {
            error(HttpStatusCode.BadRequest, "Selected city does not belong to the given country")
        }
    }

    fun getCountry(id: Long, languageCode: String = "ru"): CountryResponse? = transaction {
        val translations = CountryTranslations
            .selectAll()
            .where {
                (CountryTranslations.countryId eq id) and (CountryTranslations.languageCode eq languageCode)
            }
            .map {
                CountryTranslationResponse(
                    id = it[CountryTranslations.id],
                    countryId = id,
                    languageCode = it[CountryTranslations.languageCode],
                    name = it[CountryTranslations.name]
                )
            }

        Countries
            .selectAll()
            .where { Countries.id eq id }
            .firstOrNull()?.let {
                CountryResponse(
                    id = it[Countries.id],
                    name = it[Countries.name],
                    phoneCode = it[Countries.phoneCode],
                    isoCode = it[Countries.isoCode],
                    translations = translations,
                    city = emptyList(),
                    cityIds = emptyList()
                )
            }
    }

    fun getCountryName(id: Long): String? = transaction {
        Countries
            .selectAll()
            .where { Countries.id eq id }
            .map { it[Countries.name] }
            .firstOrNull()
    }

    fun getCountryPhoneCode(id: Long): String? = transaction {
        Countries
            .selectAll()
            .where { Countries.id eq id }
            .map { it[Countries.phoneCode] }
            .firstOrNull()
    }

    fun getCountryIsoCode(id: Long): String? = transaction {
        Countries
            .selectAll()
            .where { Countries.id eq id }
            .map { it[Countries.isoCode] }
            .firstOrNull()
    }

    fun getCity(cityId: Long, languageCode: String = "ru"): CityResponse? = transaction {
        val translations = CityTranslations
            .selectAll()
            .where {
                (CityTranslations.cityId eq cityId) and
                        (CityTranslations.languageCode eq languageCode)
            }
            .map {
                CityTranslationResponse(
                    id = it[CityTranslations.id],
                    cityId = cityId,
                    languageCode = it[CityTranslations.languageCode],
                    name = it[CityTranslations.name]
                )
            }

        Cities
            .selectAll()
            .where { Cities.id eq cityId }
            .firstOrNull()?.let {
                CityResponse(
                    id = it[Cities.id],
                    name = it[Cities.name],
                    countryId = it[Cities.countryId],
                    translations = translations
                )
            }
    }

    fun getCityName(cityId: Long): String? = transaction {
        Cities
            .selectAll()
            .where { Cities.id eq cityId }
            .map { it[Cities.name] }
            .firstOrNull()
    }

    fun formatAddressString(it: CounterpartyAddressResponse): String =
        listOfNotNull(
            it.countryName,
            it.cityName,
            it.streetName,
            it.houseNumber,
            it.locationNumber
        ).joinToString(", ")
}