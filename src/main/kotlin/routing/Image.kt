package com.example.routing

import com.example.LocalImageStorageService
import com.example.data.ProductDao
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.http.content.*

fun Route.imageRoutes() {

    post("/products/{id}/images") {
        val productId = call.parameters["id"]?.toLongOrNull()
        if (productId == null) {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Некорректный ID товара"))
            return@post
        }

        val multipart = call.receiveMultipart()
        val imageStorage = LocalImageStorageService()
        var savedPath: String? = null
        var hasError = false
        var errorMessage: String? = null

        multipart.forEachPart { part ->
            if (part is PartData.FileItem && part.name == "image") {
                try {
                    val path = imageStorage.saveImage(productId, part)
                    ProductDao.insertProductImagePath(productId, path)
                    savedPath = path
                } catch (e: Exception) {
                    hasError = true
                    errorMessage = "Ошибка при сохранении: ${e.localizedMessage}"
                }
            }
            part.dispose()
        }

        if (hasError) {
            call.respond(HttpStatusCode.InternalServerError, mapOf("error" to (errorMessage ?: "Неизвестная ошибка")))
        } else if (savedPath != null) {
            call.respond(HttpStatusCode.Created, mapOf("message" to "Изображение загружено", "path" to savedPath))
        } else {
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Файл не был передан"))
        }
    }
}
