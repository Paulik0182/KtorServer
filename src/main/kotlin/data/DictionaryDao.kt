package com.example.data

import com.example.Cities
import com.example.CityTranslations
import com.example.Countries
import com.example.CountryTranslations
import com.example.data.dto.dictionaries.CityResponse
import com.example.data.dto.dictionaries.CityTranslationResponse
import com.example.data.dto.dictionaries.CountryResponse
import com.example.data.dto.dictionaries.CountryTranslationResponse
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object DictionaryDao {

    fun getAllCountries(languageCode: String = "ru"): List<CountryResponse> = transaction {
        Countries.selectAll().map { countryRow ->
            val countryId = countryRow[Countries.id]

            val translations = CountryTranslations
                .selectAll()
                .where { CountryTranslations.countryId eq countryId and (CountryTranslations.languageCode eq languageCode) }
                .map {
                    CountryTranslationResponse(
                        id = it[CountryTranslations.id],
                        countryId = it[CountryTranslations.countryId],
                        languageCode = it[CountryTranslations.languageCode],
                        name = it[CountryTranslations.name]
                    )
                }

            val cities = Cities.selectAll().where { Cities.countryId eq countryId }.map { cityRow ->
                CityResponse(
                    id = cityRow[Cities.id],
                    name = cityRow[Cities.name],
                    countryId = countryId,
                    translations = emptyList(),
                    country = emptyList()
                )
            }

            CountryResponse(
                id = countryId,
                name = countryRow[Countries.name],
                phoneCode = countryRow[Countries.phoneCode],
                isoCode = countryRow[Countries.isoCode],
                translations = translations,
                city = cities,
                cityIds = cities.mapNotNull { it.id }
            )
        }
    }

    fun getAllCities(languageCode: String = "ru"): List<CityResponse> = transaction {
        Cities.selectAll().map { cityRow ->
            val cityId = cityRow[Cities.id]
            val countryId = cityRow[Cities.countryId]

            val translations = CityTranslations
                .selectAll()
                .where { CityTranslations.cityId eq cityId and (CityTranslations.languageCode eq languageCode) }
                .map {
                    CityTranslationResponse(
                        id = it[CityTranslations.id],
                        cityId = it[CityTranslations.cityId],
                        languageCode = it[CityTranslations.languageCode],
                        name = it[CityTranslations.name]
                    )
                }

            val countryRow = Countries.selectAll().where { Countries.id eq countryId }.firstOrNull()
            val country = countryRow?.let {
                listOf(
                    CountryResponse(
                        id = it[Countries.id],
                        name = it[Countries.name],
                        phoneCode = it[Countries.phoneCode],
                        isoCode = it[Countries.isoCode],
                        translations = emptyList(),
                        city = emptyList(),
                        cityIds = emptyList()
                    )
                )
            } ?: emptyList()

            CityResponse(
                id = cityId,
                name = cityRow[Cities.name],
                countryId = countryId,
                translations = translations,
                country = country
            )
        }
    }
}

