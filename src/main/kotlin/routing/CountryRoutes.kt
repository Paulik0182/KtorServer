package com.example.routing

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

    }
}
