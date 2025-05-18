package com.example.data.dto.user

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String)

suspend fun ApplicationCall.respondUnauthorized(message: String) {
    respond(
        TextContent(
            Json.encodeToString(ErrorResponse.serializer(), ErrorResponse(message)),
            contentType = ContentType.Application.Json,
            status = HttpStatusCode.Unauthorized
        )
    )
}
