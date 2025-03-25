package com.example.data

import io.ktor.http.*

class DuplicateProductNameException(val productName: String, existingProductId: Long) :
    RuntimeException("Товар с названием '$productName' уже существует (ID:$existingProductId)")

class CustomHttpException(val status: HttpStatusCode, val response: Any) : RuntimeException()

fun error(status: HttpStatusCode, response: Any): Nothing {
    throw CustomHttpException(status, response)
}