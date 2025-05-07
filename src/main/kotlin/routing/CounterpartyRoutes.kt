package com.example.routing

import com.example.data.CounterpartyDao
import com.example.data.dto.counterparty.*
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

        patch("/{id}/contacts") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@patch
            }

            val contacts = call.receive<List<CounterpartyContactRequest>>()
            try {
                CounterpartyDao.updateContacts(id, contacts)
                call.respond(HttpStatusCode.OK, "Контакты обновлены")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        patch("/{id}/basic") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@patch
            }

            val patchRequest = call.receive<CounterpartyPatchRequest>()
            try {
                CounterpartyDao.updateBasicFields(id, patchRequest)
                call.respond(HttpStatusCode.OK, "Базовые поля обновлены")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        patch("/{id}/addresses") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@patch
            }

            val patchRequest = call.receive<List<CounterpartyAddressRequest>>()
            try {
                CounterpartyDao.updateAddresses(id, patchRequest)
                call.respond(HttpStatusCode.OK, "Адрес обновлен")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        patch("/{id}/representatives") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@patch
            }

            val patchRequest = call.receive<RepresentativeRequest>()
            try {
                CounterpartyDao.updateRepresentatives(id, patchRequest)
                call.respond(HttpStatusCode.OK, "Данные обновлены")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        patch("/{id}/bankAccounts") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@patch
            }

            val patchRequest = call.receive<BankAccountRequest>()
            try {
                CounterpartyDao.updateBankAccounts(id, patchRequest)
                call.respond(HttpStatusCode.OK, "Банковские данные обновлены")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        patch("/{id}/image") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@patch
            }

            val patchRequest = call.receive<CounterpartyImageRequest>()
            try {
                CounterpartyDao.updateImagePath(id, patchRequest.imagePath)
                call.respond(HttpStatusCode.OK, "Аватар обновлён")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }
    }
}