package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
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

//fun Application.module() {
//    install(ContentNegotiation) {
//        json()
//    }
//    configureRouting()
//}

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true // форматирует JSON в читаемом виде (удобно для отладки).
            isLenient = true // позволяет парсить JSON, даже если он не полностью соответствует спецификации.
            ignoreUnknownKeys = true // позволяет игнорировать неизвестные ключи в JSON-ответе
        })
    }

    DatabaseFactory.init() // Подключаем базу данных

    configureRouting() // Настраиваем маршруты
}