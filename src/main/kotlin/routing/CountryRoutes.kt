package com.example.routing

import com.example.data.AddressDao
import com.example.data.CountryDao
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.countryRoutes() {
    route("/countries") {

        get("") {
            try {
                val languageCode = call.request.queryParameters["lang"] ?: "ru"
                val result = CountryDao.getAll(languageCode)
                call.respond(result)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Ошибка при получении списка стран: ${e.localizedMessage}"
                )
            }
        }

        get("/cities/{countryId}") {
            try {
                val languageCode = call.request.queryParameters["lang"] ?: "ru"
                val countryId = call.parameters["countryId"]?.toLongOrNull()
                    ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Неверный идентификатор страны"
                    )

                val result = AddressDao.getCitiesByCountry(
                    countryId = countryId,
                    languageCode = languageCode
                )
                call.respond(result)
            } catch (e: Exception) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Ошибка при получении списка городов: ${e.localizedMessage}"
                )
            }
        }
    }
}
