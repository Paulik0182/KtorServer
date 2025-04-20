package com.example.routing

import com.example.data.CounterpartyDao
import com.example.data.dto.counterparty.CounterpartyRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.counterpartyRoutes() {
    route("/counterparties") {

        get("/all") {
            try {
                val result = CounterpartyDao.getAll()
                call.respond(result)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Ошибка при получении контрагентов: ${e.localizedMessage}"
                )
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID контрагента не передан или некорректен")
                return@get
            }

            val counterparty = CounterpartyDao.getById(id)
            if (counterparty == null) {
                call.respond(HttpStatusCode.NotFound, "Контрагент не найден")
            } else {
                call.respond(counterparty)
            }
        }

        post {
            try {
                val request = call.receive<CounterpartyRequest>()
                val id = CounterpartyDao.insert(request)
                call.respond(HttpStatusCode.Created, mapOf("id" to id))
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, "Ошибка при создании контрагента: ${e.localizedMessage}")
            }
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@put
            }

            val request = call.receive<CounterpartyRequest>()
            try {
                CounterpartyDao.update(id, request)
                call.respond(HttpStatusCode.OK, "Контрагент обновлён")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@delete
            }

            try {
                CounterpartyDao.delete(id)
                call.respond(HttpStatusCode.OK, "Контрагент удалён")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при удалении: ${e.localizedMessage}")
            }
        }
    }
}
