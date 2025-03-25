package com.example

import com.example.data.*
import com.example.data.dto.*
import com.example.data.dto.counterparty.CounterpartyResponse
import com.example.data.dto.order.OrderItemResponse
import com.example.data.dto.order.OrderResponse
import com.example.data.dto.product.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.ContentTransformationException
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.Base64

/**
 * Здесь объявляются маршруты (GET, POST, DELETE и др.) (endpoints) API, позволяя серверу принимать запросы и отвечать на них.
 */
fun Application.configureRouting() {

    /**
     * routing {} – блок, в котором определяются маршруты (endpoints).
     * get("/") {} – обработчик GET-запроса.
     * post("/products") {} – обработчик POST-запроса.
     * delete("/orders/{id}") {} – обработчик DELETE-запроса.
     */
    routing {
        // Главная страница
        get("/") {
            call.respondText("Hello World!")
        }

        // Простая математическая операция
        get("/sum") {
            val a = call.request.queryParameters["a"]?.toDoubleOrNull()
            val b = call.request.queryParameters["b"]?.toDoubleOrNull()

            if (a == null || b == null) {
                call.respondText("Недопустимые параметры, укажите цыфры")
            } else {
                val sum = a + b
                call.respondText("Сумма $a и $b = $sum")
                // http://127.0.0.1:8080/sum?a=5&b=10
            }
        }

        // Определение IP
        get("/ip") {
            val ip = call.request.origin.remoteHost
            call.respondText("Your IP is $ip")
            // http://127.0.0.1/:8080/ip
        }

        /**
        * TODO Получить список всех товаров  !!Переделан!!
         * */
        get("/products") {
            try {
                val products = ProductDao.getAll()
                call.respond(products)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }
        // Тестовый запрос: http://127.0.0.1:8080/products

        /**
         * TODO Получить товар по ID  !!Переделан!!
         */
        get("/products/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара")

            try {
                val product = ProductDao.getById(id)
                if (product != null) {
                    call.respond(product)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Товар не найден")
                }
//                val product = ProductDao.getById(id) ?: return@get call.respond(
//                    HttpStatusCode.NotFound, "Продукт не найден"
//                )
//                call.respond(product)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }
        // Тестовый запрос: http://127.0.0.1:8080/products/5

        /**
         * TODO Добавить товар !!Переделан!!
         */
        post("/products") {
            try {
                val product = call.receive<ProductCreateRequest>()
                val newId = ProductDao.insert(product)
                call.respond(HttpStatusCode.Created, mapOf("Продукт создан с ID = " to newId))
            } catch (e: DuplicateProductNameException) {
                call.respond(
                    HttpStatusCode.Conflict, // 409 - конфликт данных
                    mapOf("error" to "duplicate_name", "message" to e.message)
                )
            }
        }
        // Тестовый запрос (cURL):
        // curl -X POST http://127.0.0.1:8080/products -H "Content-Type: application/json" -d '{...}'

        // Получение кодов товара
        get("/products/{id}/codes") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Некорректный ID")

            try {
                val codes = ProductDao.getProductCodes(productId)
                call.respond(codes)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }
        // Тестовый запрос: http://127.0.0.1:8080/products/5/codes

//                val images = transaction {
//                    ProductImages.selectAll().where { ProductImages.productId eq productId }
//                        .map {
//                            ProductImageResponse(
//                                id = it[ProductImages.id],
//                                productId = it[ProductImages.productId],
//                                imageBase64 = Base64.getEncoder().encodeToString(it[ProductImages.imageBase64])
//                            )
//                        }

        // 🔹 Получение изображений товара
        get("/products/{id}/images") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Некорректный ID продукта")

            try {
                val images = ProductDao.getProductImages(productId)
                call.respond(images)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }
        // Тестовый запрос: http://127.0.0.1:8080/products/5/images

        // 🔹 Добавление изображения
        post("/products/{id}/images") {
//            val productId = call.parameters["id"]?.toLongOrNull()
//                ?: return@post call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара")
//
//            try {
//                val params = call.receive<Map<String, String>>()
//                val imageBase64 = params["image"]
//                    ?: return@post call.respond(HttpStatusCode.BadRequest, "Отсутствует изображение")
//
//                val imageBytes = Base64.getDecoder().decode(imageBase64)
//                val id = ProductDao.insertProductImages(productId, listOf(ProductImageResponse(null, productId, imageBase64)))
////                val id = ProductImageDao.insert(productId, imageBytes)
//                call.respond(HttpStatusCode.Created, "Изображение добавлено с ID = $id")
//            } catch (e: Exception) {
//                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
//            }
        }
        // Тестовый запрос (cURL):
        // curl -X POST http://127.0.0.1:8080/products/5/images -H "Content-Type: application/json" -d '{"image": "..." }'

//        get("/products/{id}/links") {
//            val productId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
//                HttpStatusCode.BadRequest, "Неверный ID продукта"
//            )
//            try {
//                val links = transaction {
//                    ProductLinks.selectAll().where { ProductLinks.productId eq productId }
//                        .map {
//                            ProductLinkResponse(
//                                id = it[ProductLinks.id],
//                                productId = it[ProductLinks.productId],
//                                counterpartyId = it[ProductLinks.counterpartyId],
//                                url = it[ProductLinks.url]
//                            )
//                        }
//                }
//                call.respond(links)
//            } catch (e: Exception) {
//                e.printStackTrace()
//                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
//            }
//        }
//
//        post("/products/{id}/links") {
//            val productId = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(
//                HttpStatusCode.BadRequest,
//                "Некорректный ID продукта"
//            )
//            val params = call.receive<Map<String, String>>()
//            val counterpartyId = params["counterpartyId"]?.toIntOrNull() ?: return@post call.respond(
//                HttpStatusCode.BadRequest,
//                "Некорректный ID контрагента"
//            )
//            val url = params["url"] ?: return@post call.respond(HttpStatusCode.BadRequest, "URL обязателен")
//
//            val id = ProductLinkDao.insert(productId, counterpartyId, url)
//            call.respond("Ссылка добавлена с ID = $id")
//        }

        // Удаление товара
        delete("/products/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@delete call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара")

            try {
                ProductDao.delete(id)
                call.respond(HttpStatusCode.OK, "Товар удален")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }
        // Тестовый запрос: DELETE http://127.0.0.1:8080/products/5

        /**
         * TODO Обновить данные товара  !!Переделан!!
         */
        put("/products/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
                ?: return@put call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_id", "message" to "Некорректный ID товара"))

            try {
                val product = call.receive<ProductCreateRequest>()
                println("Обновление продукта: ID=$id, Name=${product.name}, Categories=${product.categories}")

                ProductDao.update(id, product)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Продукт $id успешно обновлён"))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "invalid_data", "message" to e.localizedMessage))
            } catch (e: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "bad_request", "message" to "Невалидный JSON: ${e.localizedMessage}"))
            } catch (e: CustomHttpException) {
                call.respond(e.status, e.response)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "internal_error", "message" to e.localizedMessage))
            }
        }
        // curl -X PUT http://127.0.0.1:8080/products/5 -H "Content-Type: application/json" -d '{...}'

        /**
         * Получение поставщиков товара
         */
        get("/products/{id}/suppliers") {
            val productId = call.parameters["id"]?.toLongOrNull()
                ?: return@get call.respond(HttpStatusCode.BadRequest, "Некорректный ID")

            try {
                val suppliers = ProductDao.getProductSuppliers(productId)
                call.respond(suppliers)
//                call.respond(suppliers.map { it[Counterparties.name] })
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }
        // Тестовый запрос: http://127.0.0.1:8080/products/5/suppliers

        /**
         * Добавление поставщика к товару
         */
        post("/products/{productId}/suppliers/{supplierId}") {
            // Получаем ID товара и поставщика
            val productId = call.parameters["productId"]?.toLongOrNull()
            val supplierId = call.parameters["supplierId"]?.toLongOrNull()

            if (productId != null && supplierId != null) {
//                ProductDao.insertProductSuppliers(productId, listOf(ProductSupplierResponse(null, productId, supplierId)))
                call.respond(HttpStatusCode.OK, "Поставщик $supplierId добавлен к товару $productId")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара или поставщика")
            }
        }
        // Тестовый запрос: POST http://127.0.0.1:8080/products/5/suppliers/3

//        // Удалить поставщика от товара
//        delete("/products/{productId}/suppliers/{supplierId}") {
//            val productId = call.parameters["productId"]?.toIntOrNull()
//            val supplierId = call.parameters["supplierId"]?.toIntOrNull()
//            if (productId != null && supplierId != null) {
//                ProductSupplierDao.removeSupplierFromProduct(productId, supplierId)
//                call.respond("Поставщик $supplierId удалён от товара $productId")
//            } else {
//                call.respond("Неверный ID товара или поставщика")
//            }
//            // DELETE /products/5/suppliers/3
//            // Ответ: "Поставщик 3 удалён от товара 5"
//        }

        // Удаление поставщика от товара
//        delete("/products/{productId}/suppliers/{supplierId}") {
//            val productId = call.parameters["productId"]?.toLongOrNull()
//            val supplierId = call.parameters["supplierId"]?.toLongOrNull()
//
//            if (productId != null && supplierId != null) {
//                ProductDao.removeSupplierFromProduct(productId, supplierId)
//                call.respond(HttpStatusCode.OK, "Поставщик $supplierId удален от товара $productId")
//            } else {
//                call.respond(HttpStatusCode.BadRequest, "Некорректный ID товара или поставщика")
//            }
//        }
        // Тестовый запрос: DELETE http://127.0.0.1:8080/products/5/suppliers/3

        // Отправляем полный список контрагентов
        get("/counterparties") {
            try {
                val counterparties = CounterpartyDao.getAll().map {
//                    CounterpartyResponse(
//                        id = it[Counterparties.id],
//                        name = it[Counterparties.name],
//                        type = it[Counterparties.type],
//                        isSupplier = it[Counterparties.isSupplier],
//                        productCountOld = it[Counterparties.productCountOld]
//                    )
                }
                call.respond(counterparties) // сериализуется корректно
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
            // http://127.0.0.1:8080/counterparties
        }

        get("/counterparties/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, "Неверный ID контрагента"
            )

            try {
//                val counterparty = CounterpartyDao.getById(id) ?: return@get call.respond(
//                    HttpStatusCode.NotFound, "Контрагент не найден"
//                )

//                call.respond(
//                    CounterpartyResponse(
//                        id = counterparty[Counterparties.id],
//                        name = counterparty[Counterparties.name],
//                        type = counterparty[Counterparties.type],
//                        isSupplier = counterparty[Counterparties.isSupplier],
//                        productCountOld = counterparty[Counterparties.productCountOld]
//                    )
//                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        // Получить товары поставщика
        get("/counterparties/{id}/products") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
//                val products = CounterpartyDao.getProductsBySupplier(id)
//                call.respond(products.map { it[Products.name] })
            } else {
                call.respond("Неверный ID")
            }
        }

        // TODO Добавить заказчика (поставщика или клиента)
        post("/counterparties") {
            try {
                val counterparty = call.receive<CounterpartyResponse>() // Принимаем объект
//                val id = CounterpartyDao.insert(counterparty.name, counterparty.type)
//                call.respond(HttpStatusCode.Created, "Контрагент создан с ID = $id")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при добавлении: ${e.localizedMessage}")
            }
        }

        // TODO - Работает - Удалить заказчика (поставщика или клиента)
        delete("/counterparties/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
//                CounterpartyDao.deleteWithProducts(id)
//                call.respond("Контрагент $id удалён")
            } else {
                call.respond("Неверный ID контрагента")
            }
        }

        // TODO Обновить данные поставщика
        put("/counterparties/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Некорректный ID"
                )
                val counterparty = call.receive<CounterpartyResponse>()
//                CounterpartyDao.update(id, counterparty.name, counterparty.type)
//                call.respond(HttpStatusCode.OK, "Контрагент $id обновлен")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        // Получить все заказы
        get("/orders") {
            try {
                call.respond(OrderDao.getAll().map { order ->
                    // Получаем контрагента
//                    val counterparty = CounterpartyDao.getById(order[Orders.counterpartyId])

//                    OrderResponse(
//                        id = order[Orders.id],
//                        orderDate = order[Orders.orderDate].toString(),
//                        counterpartyId = order[Orders.counterpartyId],
//                        counterpartyName = counterparty?.get(Counterparties.name)
//                            ?: "Неизвестный контрагент", // 🔹 Добавили!
//                        items = OrderItemDao.getItemsByOrder(order[Orders.id]).map { row ->
//                            OrderItemResponse(
//                                id = row[OrderItems.id],
//                                orderId = row[OrderItems.orderId],
//                                productId = row[OrderItems.productId],
//                                productName = row[Products.name],
//                                supplierId = row[OrderItems.supplierId],
//                                quantity = row[OrderItems.quantity]
//                            )
//                        }
//                    )
                })
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        // получение делалей заказа
        get("/orders/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, "Неверный ID заказа")
                return@get
            }

            try {
//                val order = OrderDao.getById(id)
//                if (order == null) {
//                    call.respond(HttpStatusCode.NotFound, "Заказ не найден")
//                    return@get
//                }

                // Получаем контрагента по ID
//                val counterparty = CounterpartyDao.getById(order[Orders.counterpartyId])

                // Получаем товары для заказа
//                val items = OrderItemDao.getItemsByOrder(id).map { item ->
//                    val product = ProductDao.getById(item[OrderItems.productId])
//                    OrderItemResponse(
//                        id = item[OrderItems.id],
//                        orderId = id,
//                        productId = item[OrderItems.productId],
//                        productName = product?.name ?: "Неизвестный продукт",
//                        supplierId = item[OrderItems.supplierId],
//                        quantity = item[OrderItems.quantity]
//                    )
//                }

                // Формируем полный ответ
//                val orderResponse = OrderResponse(
//                    id = order[Orders.id],
//                    orderDate = order[Orders.orderDate].toString(), // Преобразуем дату в строку
//                    counterpartyId = order[Orders.counterpartyId],
//                    counterpartyName = counterparty?.get(Counterparties.name) ?: "Неизвестный контрагент",
//                    items = items
//                )

//                call.respond(orderResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        // Получить товары в заказе
        get("/orders/{id}/items") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
//                val items = OrderDao.getOrderItems(id)
//                call.respond(items.map { it[Products.name] })
            } else {
                call.respond("Неверный ID")
            }
        }

        // Создать новый заказ
        post("/orders") {
            try {
                val request = call.receive<OrderResponse>()
//                val orderId = OrderDao.insert(request.counterpartyId)

                request.items.forEach { item ->
//                    OrderItemDao.insert(orderId, item.productId, item.supplierId, item.quantity)
                }

//                call.respond(HttpStatusCode.Created, "Заказ $orderId создан")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }

        // Обновление заказа
        put("/orders/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                    HttpStatusCode.BadRequest, "Некорректный ID"
                )

                val request = call.receive<OrderResponse>()
//                OrderDao.update(id, request.counterpartyId)

                // Удаляем старые товары и добавляем новые
//                OrderItemDao.deleteItemsByOrder(id)
                request.items.forEach { item ->
//                    OrderItemDao.insert(id, item.productId, item.supplierId, item.quantity)
                }

                call.respond(HttpStatusCode.OK, "Заказ $id обновлен")
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Ошибка: ${e.localizedMessage}")
            }
        }

        // удаляет заказ и все связанные с ним товары в order_items.
        delete("/orders/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
//                OrderDao.deleteWithItems(id)
//                call.respond("Заказ $id удалён")
            } else {
                call.respond("Неверный ID заказа")
            }
        }
    }
}
