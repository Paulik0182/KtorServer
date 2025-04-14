package com.example.routing

import com.example.LocalImageStorageService
import com.example.data.ProductImageDao
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.http.content.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Route.imageRoutes() {

    route("/products/{id}/images") {

        get {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара")

            try {
                val images = ProductImageDao.getProductImages(productId)
                call.respond(images)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }

//        get("/{imageId}") {
//            val productId = call.parameters["id"]?.toLongOrNull()
//            val imageId = call.parameters["imageId"]?.toLongOrNull()
//
//            if (productId == null || imageId == null) {
//                call.respond(HttpStatusCode.BadRequest, "Некорректные параметры")
//                return@get
//            }
//
//            val imagePath = transaction {
//                ProductImages
//                    .selectAll().where {
//                        (ProductImages.id eq imageId) and (ProductImages.productId eq productId)
//                    }
//                    .mapNotNull { it[ProductImages.imagePath] }
//                    .firstOrNull()
//            }
//
//            if (imagePath == null || !File(imagePath).exists()) {
//                call.respond(HttpStatusCode.NotFound, "Изображение не найдено")
//            } else {
//                call.respondFile(File(imagePath))
//            }
//        }

        /** Получение одного изображения */
        get("/{imageId}") {
            val imageId = call.parameters["imageId"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Некорректный ID изображения")

            val image = ProductImageDao.getProductImageById(imageId)
                ?: return@get call.respond(HttpStatusCode.NotFound, "Изображение не найдено")

            call.respond(image)
        }

        post {
            val productId = call.parameters["id"]?.toLongOrNull()
            if (productId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Некорректный ID товара"))
                return@post
            }

            val multipart = call.receiveMultipart()
            val imageStorage = LocalImageStorageService()

            val existingImages = ProductImageDao.getProductImages(productId)
            if (existingImages.size >= 10) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Максимум 10 изображений на продукт")
                )
            }

            val parts = mutableListOf<PartData.FileItem>()
            multipart.forEachPart { part ->
                if (part is PartData.FileItem && part.name == "image") {
                    parts.add(part)
                } else {
                    part.dispose()
                }
            }
            val totalSize = parts.sumOf { it.streamProvider().available().toLong() }

            if (totalSize > 10 * 1024 * 1024) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Суммарный размер файлов не должен превышать 10MB")
                )
            }

            var savedPath: String? = null
            var hasError = false
            var errorMessage: String? = null

            parts.forEach { part ->
                try {
                    val path = imageStorage.saveImage(productId, part)
                    ProductImageDao.insertProductImagePath(productId, path)
                    savedPath = path
                } catch (e: Exception) {
                    hasError = true
                    errorMessage = "Ошибка при сохранении: ${e.localizedMessage}"
                } finally {
                    part.dispose()
                }
            }

            if (hasError) {
                call.respond(
                    HttpStatusCode.InternalServerError,
                    mapOf("error" to (errorMessage ?: "Неизвестная ошибка"))
                )
            } else if (savedPath != null) {
                call.respond(HttpStatusCode.Created, mapOf("message" to "Изображение загружено", "path" to savedPath))
            } else {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Файл не был передан"))
            }
        }

        /** Изменение порядка изображений */
        post("/reorder") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара")

            val imageIds = call.receive<List<Long>>()
            ProductImageDao.reorderProductImages(productId, imageIds)
            call.respond(HttpStatusCode.OK, "Порядок изображений обновлён")
        }

        delete("/{imageId}") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара")

            val imageId = call.parameters["imageId"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Некорректный ID изображения")

            val image = ProductImageDao.getProductImageById(imageId)
                ?: return@delete call.respond(HttpStatusCode.NotFound, "Изображение не найдено")

            if (image.productId != productId) {
                return@delete call.respond(HttpStatusCode.Forbidden, "Это изображение не принадлежит товару")
            }

            val deletedFromDb = ProductImageDao.deleteProductImage(imageId)
            val deletedFromDisk = LocalImageStorageService().deleteImage(image.imageUrl)

            if (deletedFromDb) {
                call.respond(
                    HttpStatusCode.OK,
                    mapOf(
                        "message" to "Изображение удалено",
                        "deletedFromDisk" to deletedFromDisk
                    )
                )
            } else {
                call.respond(HttpStatusCode.InternalServerError, "Не удалось удалить изображение из БД")
            }
        }

        patch {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: return@patch call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара")

            val body = call.receive<List<Long>>() // imageIds в порядке
            ProductImageDao.reorderProductImages(productId, body)

            call.respond(HttpStatusCode.OK, "Порядок обновлён")
        }
    }
}
