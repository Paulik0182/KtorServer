package com.example.routing

import com.example.JwtConfig
import com.example.data.PasswordTokenDao
import com.example.data.UserDao
import com.example.data.UserSessionDao
import com.example.data.dto.user.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.Duration
import java.util.*

fun Route.authRoutes() {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            val userId = UserDao.register(
                request.email,
                request.password,
                role = request.role,
                counterpartyId = null
            )
            call.respond(mapOf("userId" to userId))
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            try {
                val (userId, role) = UserDao.validateLogin(request.email, request.password)
                val rawToken = UUID.randomUUID().toString()
                val token = JwtConfig.makeToken(userId, role.name, rawToken)
                // Сохраняем сессию
                UserSessionDao.addSession(
                    userId = userId,
                    token = rawToken, // сохраняем в базу именно этот rawToken, не jwt
                    expiresAt = System.currentTimeMillis() + 86_400_000, // 24 часа
                    deviceInfo = request.deviceInfo
                )
                call.respond(mapOf("token" to token))
            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.Unauthorized, e.message ?: "Ошибка авторизации")
            }
        }

        // Запросить сброс пароля
        post("/request_password_reset") {
            val request = call.receive<ResetRequest>() // содержит email
            val userId = UserDao.findUserIdByEmail(request.email)
                ?: return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("Пользователь не найден"))

            val token = UUID.randomUUID().toString()
            PasswordTokenDao.storeToken(userId, token, Duration.ofMinutes(15))

            val link = "https://example.com/reset_password?token=$token"
            PasswordTokenDao.sendEmail(request.email, "Сброс пароля: $link")

            call.respond(mapOf("status" to "reset link sent"))
        }

        //  Сбросить пароль
        post("/reset_password") {
            val request = call.receive<ResetPasswordRequest>() // содержит: token + newPassword
            val userId = PasswordTokenDao.validateAndConsumeToken(request.token)
                ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Неверный или просроченный токен"))

            UserDao.updatePassword(userId, request.newPassword)
            call.respond(mapOf("status" to "password changed"))
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<UserPrincipal>()!!
                call.respond(MeResponse(principal.userId, principal.role.name))
            }

            post("/logout") {
                val principal = call.principal<UserPrincipal>()!!
                val jwt = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                    ?: return@post call.respondText("No token", status = HttpStatusCode.Unauthorized)

                val decoded = JwtConfig.verifier.verify(jwt)
                val rawToken = decoded.getClaim("token").asString()

                UserSessionDao.deleteToken(rawToken)
                call.respond(mapOf("status" to "ok"))
            }

            // Удалить все сессии
            post("/logout_all") {
                val principal = call.principal<UserPrincipal>()!!
                UserSessionDao.deleteAllSessionsForUser(principal.userId)
                call.respond(mapOf("status" to "all sessions removed"))
            }

            // Удалить все сессии, кроме текущей
            post("/logout_others") {
                val principal = call.principal<UserPrincipal>()!!
                val rawToken = call.request.headers["Authorization"]
                    ?.removePrefix("Bearer ")
                    ?.let { JwtConfig.verifier.verify(it).getClaim("token").asString() }

                if (rawToken == null) {
                    return@post call.respond(HttpStatusCode.BadRequest, "Token not found")
                }

                UserSessionDao.deleteAllSessionsExcept(principal.userId, rawToken)
                call.respond(mapOf("status" to "other sessions removed"))
            }

            // Только для администратора
            get("/admin/sessions") {
                val principal = call.principal<UserPrincipal>()!!
                if (principal.role != UserRole.SYSTEM_ADMIN) {
                    return@get call.respond(HttpStatusCode.Forbidden, "Access denied")
                }

                val sessions = UserSessionDao.getAllSessions()
                call.respond(sessions)
            }

            // Удаляем сессию конкретного пользователя
            post("/admin/logout_user") {
                val principal = call.principal<UserPrincipal>()!!
                if (principal.role != UserRole.SYSTEM_ADMIN) {
                    return@post call.respond(HttpStatusCode.Forbidden, ErrorResponse("Недостаточно прав"))
                }

                val request = call.receive<Map<String, Long>>()
                val targetUserId = request["userId"]
                    ?: return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("userId обязателен"))

                if (!UserDao.exists(targetUserId)) {
                    return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("Пользователь не найден"))
                }

                UserSessionDao.deleteAllSessionsByAdmin(targetUserId)
                call.respond(mapOf("status" to "user logged out"))
            }

            // // Удаляем все сессии и остается только админ!
            post("/admin/logout_all_except_self") {
                val principal = call.principal<UserPrincipal>()!!
                if (principal.role != UserRole.SYSTEM_ADMIN) {
                    return@post call.respond(HttpStatusCode.Forbidden, "Access denied")
                }

                UserSessionDao.deleteAllSessionsExceptUser(principal.userId)
                call.respond(mapOf("status" to "all other sessions removed"))
            }
        }
    }
}
