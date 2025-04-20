package com.example.routing


import com.example.data.DictionaryDao
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.dictionaryRoutes() {
    route("/") {

        // Получение словаря стран
        get("/dictionaries/countries") {
            try {
                val result = DictionaryDao.getAllCountries()
                call.respond(result)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при получении стран: ${e.localizedMessage}")
            }
        }

        // Получение словаря городов
        get("/dictionaries/cities") {
            try {
                val result = DictionaryDao.getAllCities()
                call.respond(result)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при получении городов: ${e.localizedMessage}")
            }
        }
    }
}