package com.example.routing

import com.example.data.AddressDao
import com.example.data.dto.counterparty.CounterpartyAddressRequest
import com.example.data.dto.user.UserPrincipal
import io.ktor.http.*
import io.ktor.server.auth.*
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
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
                }
            }
        }
    }
}