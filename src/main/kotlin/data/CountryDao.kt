package com.example.data

import com.example.Countries
import com.example.CountryTranslations
import com.example.data.dto.dictionaries.CountryResponse
import com.example.data.dto.dictionaries.CountryTranslationResponse
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object CountryDao {
    fun getAll(languageCode: String = "ru"): List<CountryResponse> = transaction {
        Countries.selectAll().map { row ->
            val countryId = row[Countries.id]

            val translations = CountryTranslations
                .selectAll()
                .where {
                    (CountryTranslations.countryId eq countryId) and
                            (CountryTranslations.languageCode eq languageCode)
                }
                .map {
                    CountryTranslationResponse(
                        id = it[CountryTranslations.id],
                        countryId = countryId,
                        languageCode = it[CountryTranslations.languageCode],
                        name = it[CountryTranslations.name]
                    )
                }

            CountryResponse(
                id = countryId,
                name = row[Countries.name],
                phoneCode = row[Countries.phoneCode],
                isoCode = row[Countries.isoCode],
                translations = translations,
                city = emptyList(),
                cityIds = emptyList()
            )
        }
    }
}
