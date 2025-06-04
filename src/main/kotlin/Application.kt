package com.example

import com.example.data.UserSessionDao
import com.example.data.dto.user.UserPrincipal
import com.example.data.dto.user.respondUnauthorized
import com.example.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * Порт 8080 — это стандартный порт для веб-приложений.
 * Можно использовать другие порты, например, 5000, 3000, но некоторые порты (например, 80) требуют прав администратора.
 * Если порт занят, сервер выдаст ошибку, поэтому перед запуском стоит убедиться, что он свободен.
 *
 * Как изменить порт через код:
 * fun main() {
 *     embeddedServer(Netty, port = 9090, module = Application::module).start(wait = true)
 * }
 */

fun Application.module() {

    DatabaseFactory.init() // Подключаем базу данных

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true // форматирует JSON в читаемом виде (удобно для отладки).
            isLenient = true // позволяет парсить JSON, даже если он не полностью соответствует спецификации.
            ignoreUnknownKeys = true // позволяет игнорировать неизвестные ключи в JSON-ответе
        })
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier) // проверка подписи токена
            validate { credential ->  // обработка валидного токена
                val userId = credential.payload.getClaim("userId").asLong()
                val role = credential.payload.getClaim("role").asString()?.let { UserRole.valueOf(it) }
                val rawToken = credential.payload.getClaim("token").asString()
                val counterpartyId = credential.payload.getClaim("counterpartyId")?.asLong()

                val sessionValid = UserSessionDao.isValidToken(userId, rawToken)
                if (sessionValid) role?.let { UserPrincipal(userId, it, counterpartyId) } else null
            }

            challenge { _, _ ->
                call.respondUnauthorized("Пользователь не залогинен")
            }
        }
    }

    routing {
        categoryRoutes()
        imageRoutes()
        counterpartyRoutes()
        dictionaryRoutes()
        countryRoutes()
        authRoutes()
        addressRoutes()

        static("/uploads") {
            files("uploads")
        }

    }

    configureRouting() // Настраиваем маршруты
}