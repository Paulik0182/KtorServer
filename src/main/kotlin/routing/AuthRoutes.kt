package com.example.routing

import com.example.JwtConfig
import com.example.data.UserDao
import com.example.data.UserSessionDao
import com.example.data.dto.user.LoginRequest
import com.example.data.dto.user.RegisterRequest
import com.example.data.dto.user.UserPrincipal
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<UserPrincipal>()!!
                call.respond(mapOf("userId" to principal.userId, "role" to principal.role))
            }

            post("/logout") {
                val principal = call.principal<UserPrincipal>()!!
                val token = call.request.headers["Authorization"]?.removePrefix("Bearer ")
                    ?: return@post call.respondText("No token", status = io.ktor.http.HttpStatusCode.Unauthorized)
                UserSessionDao.deleteToken(token)
                call.respond(mapOf("status" to "ok"))
            }
        }
    }
}
