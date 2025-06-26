package com.example.routing

import com.example.data.AddressDao
import com.example.data.dto.counterparty.CounterpartyAddressRequest
import com.example.data.dto.user.UserPrincipal
import com.example.data.error.addres.AddressErrorResponse
import com.example.data.error.addres.AddressValidationException
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.addressRoutes() {
    route("/counterparties") {

        authenticate("auth-jwt") {
            patch("/{id}/addresses") {

                val principal = call.principal<UserPrincipal>()!!
                val id = call.parameters["id"]?.toLongOrNull()

                if (principal.counterpartyId != id && principal.role != UserRole.SYSTEM_ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Недостаточно прав для получения данных")
                    return@patch
                }

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                    return@patch
                }

                val patchRequest = call.receive<List<CounterpartyAddressRequest>>()
                try {
                    AddressDao.updateAddresses(id, patchRequest)
                    call.respond(HttpStatusCode.OK, "Адрес обновлен")
                } catch (e: AddressValidationException) {
                    call.respond(HttpStatusCode.BadRequest, AddressErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        AddressErrorResponse("internal_error", "Внутренняя ошибка")
                    )
                }
            }

            get("/{id}/addresses") {
                val principal = call.principal<UserPrincipal>()!!
                val id = call.parameters["id"]?.toLongOrNull()

                if (principal.counterpartyId != id && principal.role != UserRole.SYSTEM_ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    return@get
                }

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                    return@get
                }

                val result = AddressDao.getCounterpartyAddresses(id)
                call.respond(result)
            }

            get("/{id}/addresses/{addressId}") {
                val principal = call.principal<UserPrincipal>()!!
                val id = call.parameters["id"]?.toLongOrNull()
                val addressId = call.parameters["addressId"]?.toLongOrNull()

                if (principal.counterpartyId != id && principal.role != UserRole.SYSTEM_ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    return@get
                }

                if (id == null || addressId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                    return@get
                }

                val address = AddressDao.getAddressById(addressId)
                if (address == null || address.counterpartyId != id) {
                    call.respond(HttpStatusCode.NotFound, "Адрес не найден")
                    return@get
                }

                call.respond(address)
            }

            post("/{id}/addresses") {
                val principal = call.principal<UserPrincipal>()!!
                val id = call.parameters["id"]?.toLongOrNull()

                if (principal.counterpartyId != id && principal.role != UserRole.SYSTEM_ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    return@post
                }

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                    return@post
                }

                val request = call.receive<CounterpartyAddressRequest>()
                try {
                    val addressId = AddressDao.addAddress(id, request)
                    call.respond(HttpStatusCode.Created, mapOf("id" to addressId))
                } catch (e: AddressValidationException) {
                    call.respond(HttpStatusCode.BadRequest, AddressErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        AddressErrorResponse("internal_error", "Внутренняя ошибка")
                    )
                }
            }

            delete("/{id}/addresses/{addressId}") {
                val principal = call.principal<UserPrincipal>()!!
                val id = call.parameters["id"]?.toLongOrNull()
                val addressId = call.parameters["addressId"]?.toLongOrNull()

                if (principal.counterpartyId != id && principal.role != UserRole.SYSTEM_ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    return@delete
                }

                if (id == null || addressId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                    return@delete
                }

                val success = AddressDao.deleteAddress(id, addressId)
                if (success) {
                    call.respond(HttpStatusCode.OK, "Адрес удалён")
                } else {
                    call.respond(HttpStatusCode.NotFound, "Адрес не найден")
                }
            }

            patch("/{id}/addresses/{addressId}") {
                val principal = call.principal<UserPrincipal>()!!
                val id = call.parameters["id"]?.toLongOrNull()
                val addressId = call.parameters["addressId"]?.toLongOrNull()

                if (principal.counterpartyId != id && principal.role != UserRole.SYSTEM_ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Нет доступа")
                    return@patch
                }

                if (id == null || addressId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                    return@patch
                }

                val patchData = call.receive<Map<String, @UnsafeVariance Any?>>()

                try {
                    AddressDao.patchAddress(id, addressId, patchData)
                    call.respond(HttpStatusCode.OK, "Адрес обновлён")
                } catch (e: AddressValidationException) {
                    call.respond(HttpStatusCode.BadRequest, AddressErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        AddressErrorResponse("internal_error", "Внутренняя ошибка")
                    )
                }
            }

            put("/{id}/addresses/{addressId}") {
                val principal = call.principal<UserPrincipal>()!!
                val counterpartyId = call.parameters["id"]?.toLongOrNull()
                val addressId = call.parameters["addressId"]?.toLongOrNull()

                // Проверка прав доступа
                if (principal.counterpartyId != counterpartyId && principal.role != UserRole.SYSTEM_ADMIN) {
                    call.respond(HttpStatusCode.Forbidden, "Недостаточно прав")
                    return@put
                }

                // Валидация ID
                if (counterpartyId == null || addressId == null) {
                    call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                    return@put
                }

                try {
                    // Получаем данные запроса
                    val request = call.receive<CounterpartyAddressRequest>()

                    // Обновляем адрес
                    AddressDao.updateAddress(counterpartyId, addressId, request)

                    // Возвращаем успешный ответ
                    call.respond(HttpStatusCode.OK, "Адрес успешно обновлён")
                } catch (e: AddressValidationException) {
                    call.respond(HttpStatusCode.BadRequest, AddressErrorResponse(e.code, e.message))
                } catch (e: Exception) {
                    when (e) {
                        is NotFoundException -> call.respond(HttpStatusCode.NotFound, "Адрес не найден")
                        else -> {
                            e.printStackTrace()
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                AddressErrorResponse("internal_error", "Внутренняя ошибка сервера")
                            )
                        }
                    }
                }
            }
        }
    }
}
