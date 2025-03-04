package com.example

import com.example.data.*
import com.example.data.dto.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
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
         */
        get("/products") {
            try {
                val products = ProductDao.getAll()
                call.respond(products)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        /**
         * TODO Получить товар по ID  !!Переделан!!
         */
        get("/products/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, "Неверный ID продукта"
            )

            try {
                val product = ProductDao.getById(id) ?: return@get call.respond(
                    HttpStatusCode.NotFound, "Продукт не найден"
                )
                call.respond(product)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        /**
         * TODO Добавить товар !!Переделан!!
         *
         * Пример запроса:
         * POST /products
         * {
         *   "name": "Мышь",
         *   "description": "Игровая мышь",
         *   "price": "49.99"
         * }
         */
        post("/products") {
            try {
                val product = call.receive<ProductResponse>() // Безопасный способ десериализации
                val id = ProductDao.insert(
                    product.name,
                    product.description,
                    product.price,
                    product.stockQuantity,
                    product.minStockQuantity,
                    product.productCodes,
                    product.isDemanded,
                    product.productLinks,
                    product.locations,
                    product.images,
                    product.categories,
                    product.subcategories
                )
                call.respond("Продукт создан с ID = $id")
            } catch (e: Exception) {
                println("Ошибка в POST /products: ${e.localizedMessage}")
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при создании: ${e.localizedMessage}")
            }
        }

        get("/warehouse_locations") {
            try {
                val locations = transaction {
                    WarehouseLocations.selectAll().map {
                        WarehouseLocationResponse(
                            id = it[WarehouseLocations.id],
                            counterpartyId = it[WarehouseLocations.counterpartyId],
                            locationCode = it[WarehouseLocations.locationCode]
                        )
                    }
                }
                call.respond(locations)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        post("/warehouse_locations") {
            try {
                val params = call.receive<Map<String, String>>()
                val counterpartyId = params["counterpartyId"]?.toIntOrNull() ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "Некорректный ID контрагента"
                )
                val locationCode = params["locationCode"] ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    "Код склада обязателен"
                )

                val id = WarehouseLocationDao.insert(counterpartyId, locationCode)
                call.respond("Местоположение склада создано с ID = $id")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        get("/products/{id}/locations") {
            val productId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, "Неверный ID продукта"
            )
            try {
                val locations = transaction {
                    ProductLocations.innerJoin(WarehouseLocations)
                        .selectAll().where { ProductLocations.productId eq productId }
                        .map {
                            WarehouseLocationResponse(
                                id = it[WarehouseLocations.id],
                                counterpartyId = it[WarehouseLocations.counterpartyId],
                                locationCode = it[WarehouseLocations.locationCode]
                            )
                        }
                }
                call.respond(locations)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        get("/products/{id}/images") {
            val productId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, "Неверный ID продукта"
            )
            try {
                val images = transaction {
                    ProductImages.selectAll().where { ProductImages.productId eq productId }
                        .map {
                            ProductImageResponse(
                                id = it[ProductImages.id],
                                productId = it[ProductImages.productId],
                                imageBase64 = Base64.getEncoder().encodeToString(it[ProductImages.image])
                            )
                        }
                }
                call.respond(images)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        post("/products/{id}/images") {
            val productId = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Некорректный ID продукта"
            )
            val params = call.receive<Map<String, String>>()
            val imageBase64 =
                params["image"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Отсутствует изображение")

            val imageBytes = Base64.getDecoder().decode(imageBase64)
            try {
                val id = ProductImageDao.insert(productId, imageBytes)
                call.respond("Изображение добавлено с ID = $id")
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Ошибка загрузки изображения")
            }
        }

        get("/products/{id}/links") {
            val productId = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, "Неверный ID продукта"
            )
            try {
                val links = transaction {
                    ProductLinks.selectAll().where { ProductLinks.productId eq productId }
                        .map {
                            ProductLinkResponse(
                                id = it[ProductLinks.id],
                                productId = it[ProductLinks.productId],
                                counterpartyId = it[ProductLinks.counterpartyId],
                                url = it[ProductLinks.url]
                            )
                        }
                }
                call.respond(links)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        post("/products/{id}/links") {
            val productId = call.parameters["id"]?.toIntOrNull() ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Некорректный ID продукта"
            )
            val params = call.receive<Map<String, String>>()
            val counterpartyId = params["counterpartyId"]?.toIntOrNull() ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                "Некорректный ID контрагента"
            )
            val url = params["url"] ?: return@post call.respond(HttpStatusCode.BadRequest, "URL обязателен")

            val id = ProductLinkDao.insert(productId, counterpartyId, url)
            call.respond("Ссылка добавлена с ID = $id")
        }

        // Удалить товар
        delete("/products/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                ProductDao.delete(id)
                call.respond("Товар $id удалён")
            } else {
                call.respond("Неверный ID товара")
            }
        }

        /**
         * Удаление товара с очисткой связей
         */
        delete("/products/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                ProductDao.deleteWithSuppliers(id)
                call.respond("Продукт удалён")
            } else {
                call.respond("Неверный ID")
            }
        }
        // http://127.0.0.1:8080/products/55

        // TODO Обновить данные товара  !!Переделан!!
        put("/products/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Некорректный ID"
                )
                val product = call.receive<ProductResponse>()

                // Логируем полученные данные
                println("Обновление продукта: ID=$id, Name=${product.name}, Categories=${product.categories}, Subcategories=${product.subcategories}")

                ProductDao.update(
                    id,
                    product.name,
                    product.description,
                    product.price,
                    product.stockQuantity,
                    product.minStockQuantity,
                    product.productCodes,
                    product.isDemanded,
                    product.productLinks,
                    product.locations,
                    product.images,
                    product.categories,
                    product.subcategories
                )

                call.respond(HttpStatusCode.OK, "Продукт $id обновлен")

            } catch (e: Exception) {
                println("Ошибка в PUT /products: ${e.localizedMessage}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при обновлении: ${e.localizedMessage}")
            }
        }

        /**
         * Получить поставщиков товара
         *
         * Маршрут возвращает список поставщиков для конкретного товара.
         * val id = call.parameters["id"]?.toIntOrNull()
         *
         * Получаем id товара из URL (/products/5/suppliers → id = 5).
         * Преобразуем в Int. Если ошибка – null.
         * if (id != null) {
         * Проверяем, передан ли корректный ID товара.
         * val suppliers = ProductSupplierDao.getSuppliersByProduct(id)
         *
         * Вызываем метод getSuppliersByProduct(id), который:
         * Ищет всех поставщиков, привязанных к товару.
         * call.respond(suppliers.map { it[Counterparties.name] })
         *
         * Отправляем список названий поставщиков.
         * Как вызвать?
         * http://127.0.0.1:8080/products/3/suppliers
         * Ответ: ["Компания A", "Компания B"]
         */
        get("/products/{id}/suppliers") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val suppliers = ProductSupplierDao.getSuppliersByProduct(id)
                call.respond(suppliers.map { it[Counterparties.name] })
            } else {
                call.respond("Неверный ID")
            }
        }

        // Добавить поставщика к товару
        post("/products/{productId}/suppliers/{supplierId}") {
            // Получаем ID товара и поставщика
            val productId = call.parameters["productId"]?.toIntOrNull()
            val supplierId = call.parameters["supplierId"]?.toIntOrNull()

            if (productId != null && supplierId != null) {
                ProductSupplierDao.addSupplierToProduct(productId, supplierId)
                call.respond("Поставщик $supplierId добавлен к товару $productId")
            } else {
                call.respond("Неверный ID товара или поставщика")
            }
            // POST /products/5/suppliers/3
            // Ответ: "Поставщик 3 добавлен к товару 5"
        }

        // Удалить поставщика от товара
        delete("/products/{productId}/suppliers/{supplierId}") {
            val productId = call.parameters["productId"]?.toIntOrNull()
            val supplierId = call.parameters["supplierId"]?.toIntOrNull()
            if (productId != null && supplierId != null) {
                ProductSupplierDao.removeSupplierFromProduct(productId, supplierId)
                call.respond("Поставщик $supplierId удалён от товара $productId")
            } else {
                call.respond("Неверный ID товара или поставщика")
            }
            // DELETE /products/5/suppliers/3
            // Ответ: "Поставщик 3 удалён от товара 5"
        }


        // Отправляем полный список контрагентов
        get("/counterparties") {
            try {
                val counterparties = CounterpartyDao.getAll().map {
                    CounterpartyResponse(
                        id = it[Counterparties.id],
                        name = it[Counterparties.name],
                        type = it[Counterparties.type],
                        isSupplier = it[Counterparties.isSupplier],
                        productCount = it[Counterparties.productCount]
                    )
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
                val counterparty = CounterpartyDao.getById(id) ?: return@get call.respond(
                    HttpStatusCode.NotFound, "Контрагент не найден"
                )

                call.respond(
                    CounterpartyResponse(
                        id = counterparty[Counterparties.id],
                        name = counterparty[Counterparties.name],
                        type = counterparty[Counterparties.type],
                        isSupplier = counterparty[Counterparties.isSupplier],
                        productCount = counterparty[Counterparties.productCount]
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        // Получить товары поставщика
        get("/counterparties/{id}/products") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val products = CounterpartyDao.getProductsBySupplier(id)
                call.respond(products.map { it[Products.name] })
            } else {
                call.respond("Неверный ID")
            }
        }

        // TODO Добавить заказчика (поставщика или клиента)
        post("/counterparties") {
            try {
                val counterparty = call.receive<CounterpartyResponse>() // Принимаем объект
                val id = CounterpartyDao.insert(counterparty.name, counterparty.type)
                call.respond(HttpStatusCode.Created, "Контрагент создан с ID = $id")
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка при добавлении: ${e.localizedMessage}")
            }
        }

        // TODO - Работает - Удалить заказчика (поставщика или клиента)
        delete("/counterparties/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                CounterpartyDao.deleteWithProducts(id)
                call.respond("Контрагент $id удалён")
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
                CounterpartyDao.update(id, counterparty.name, counterparty.type)
                call.respond(HttpStatusCode.OK, "Контрагент $id обновлен")
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
                    val counterparty = CounterpartyDao.getById(order[Orders.counterpartyId])

                    OrderResponse(
                        id = order[Orders.id],
                        orderDate = order[Orders.orderDate].toString(),
                        counterpartyId = order[Orders.counterpartyId],
                        counterpartyName = counterparty?.get(Counterparties.name)
                            ?: "Неизвестный контрагент", // 🔹 Добавили!
                        items = OrderItemDao.getItemsByOrder(order[Orders.id]).map { row ->
                            OrderItemResponse(
                                id = row[OrderItems.id],
                                orderId = row[OrderItems.orderId],
                                productId = row[OrderItems.productId],
                                productName = row[Products.name],
                                supplierId = row[OrderItems.supplierId],
                                quantity = row[OrderItems.quantity]
                            )
                        }
                    )
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
                val order = OrderDao.getById(id)
                if (order == null) {
                    call.respond(HttpStatusCode.NotFound, "Заказ не найден")
                    return@get
                }

                // Получаем контрагента по ID
                val counterparty = CounterpartyDao.getById(order[Orders.counterpartyId])

                // Получаем товары для заказа
                val items = OrderItemDao.getItemsByOrder(id).map { item ->
                    val product = ProductDao.getById(item[OrderItems.productId])
                    OrderItemResponse(
                        id = item[OrderItems.id],
                        orderId = id,
                        productId = item[OrderItems.productId],
                        productName = product?.name ?: "Неизвестный продукт",
                        supplierId = item[OrderItems.supplierId],
                        quantity = item[OrderItems.quantity]
                    )
                }

                // Формируем полный ответ
                val orderResponse = OrderResponse(
                    id = order[Orders.id],
                    orderDate = order[Orders.orderDate].toString(), // Преобразуем дату в строку
                    counterpartyId = order[Orders.counterpartyId],
                    counterpartyName = counterparty?.get(Counterparties.name) ?: "Неизвестный контрагент",
                    items = items
                )

                call.respond(orderResponse)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        // Получить товары в заказе
        get("/orders/{id}/items") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id != null) {
                val items = OrderDao.getOrderItems(id)
                call.respond(items.map { it[Products.name] })
            } else {
                call.respond("Неверный ID")
            }
        }

        // Создать новый заказ
        post("/orders") {
            try {
                val request = call.receive<OrderResponse>()
                val orderId = OrderDao.insert(request.counterpartyId)

                request.items.forEach { item ->
                    OrderItemDao.insert(orderId, item.productId, item.supplierId, item.quantity)
                }

                call.respond(HttpStatusCode.Created, "Заказ $orderId создан")
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
                OrderDao.update(id, request.counterpartyId)

                // Удаляем старые товары и добавляем новые
                OrderItemDao.deleteItemsByOrder(id)
                request.items.forEach { item ->
                    OrderItemDao.insert(id, item.productId, item.supplierId, item.quantity)
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
                OrderDao.deleteWithItems(id)
                call.respond("Заказ $id удалён")
            } else {
                call.respond("Неверный ID заказа")
            }
        }
    }
}
