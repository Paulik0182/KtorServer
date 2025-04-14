package com.example.routing

import com.example.Categories
import com.example.LocalImageStorageService
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import com.example.data.CategoryDao
import com.example.data.dto.category.CategoryRequest
import io.ktor.http.content.*
import io.ktor.server.request.*
import org.jetbrains.exposed.sql.update

/**
 * GET /categories/all
 * GET /categories/{id}
 * POST /categories
 * PUT /categories/{id}
 * PATCH /categories/{id}
 * DELETE /categories/{id}
 * GET /subcategories
 * TODO Все запросы протестированы и работают!
 */

fun Route.categoryRoutes() {

    route("/categories") {
        get("/all") {
            try {
                val all = CategoryDao.getAllCategoriesWithSubcategories()
                call.respond(all)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(
                    HttpStatusCode.InternalServerError,
                    "Ошибка при получении категорий: ${e.localizedMessage}"
                )
            }
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "ID категории не передан")
                return@get
            }

            val category = CategoryDao.getById(id)
            if (category == null) {
                call.respond(HttpStatusCode.NotFound, "Категория не найдена")
            } else {
                call.respond(category)
            }
        }

        post {
            val request = call.receive<CategoryRequest>()
            val id = CategoryDao.insertCategory(request)
            call.respond(HttpStatusCode.Created, mapOf("id" to id))
        }

        put("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@put
            }

            val request = call.receive<CategoryRequest>()
            val category = CategoryDao.getCategoryById(id)
            if (category == null) {
                call.respond(HttpStatusCode.NotFound, "Категория не найдена")
                return@put
            }

            CategoryDao.updateCategory(id, request)
            call.respond(HttpStatusCode.OK, "Категория обновлена")
        }

        delete("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@delete
            }

            val deleted = CategoryDao.deleteCategory(id)
            if (deleted) {
                call.respond(HttpStatusCode.OK, "Категория удалена")
            } else {
                call.respond(HttpStatusCode.NotFound, "Категория не найдена")
            }
        }

        patch("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID")
                return@patch
            }

            val patch = call.receive<CategoryRequest>() // тот же формат
            CategoryDao.patchCategory(id, patch)
            call.respond(HttpStatusCode.OK, "Категория частично обновлена")
        }

        get("/subcategories") {
            val result = CategoryDao.getAllSubcategories()
            call.respond(result)
        }
    }

    post("/{id}/image") {
        val categoryId = call.parameters["id"]?.toLongOrNull() ?: return@post call.respond(HttpStatusCode.BadRequest)
        val multipart = call.receiveMultipart()
        var savedPath: String? = null
        val imageStorage = LocalImageStorageService()
        val prefix = "categories"
        multipart.forEachPart { part ->
            if (part is PartData.FileItem && part.name == "image") {
                savedPath = imageStorage.saveImage(prefix, categoryId, part)
                part.dispose()
            }
        }

        if (savedPath != null) {
            Categories.update({ Categories.id eq categoryId }) {
                it[imagePath] = savedPath!!
            }
            call.respond(HttpStatusCode.OK, mapOf("imagePath" to savedPath))
        } else {
            call.respond(HttpStatusCode.BadRequest, "Файл не передан")
        }
    }

}
