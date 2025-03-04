package com.example

import com.example.data.*
import com.example.data.dto.CounterpartyResponse
import com.example.data.dto.OrderItemResponse
import com.example.data.dto.OrderResponse
import com.example.data.dto.ProductResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
         * Получить список всех товаров TODO Переделан!!
         */
        get("/products") {
            try {
                val products = ProductDao.getAll().map {
                    ProductResponse(
                        id = it[Products.id],
                        name = it[Products.name],
                        description = it[Products.description],
                        price = it[Products.price],
                        hasSuppliers = it[Products.hasSuppliers],
                        supplierCount = it[Products.supplierCount]
                    )
                }
                call.respond(products)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        /**
         * Получить товар по ID
         */
        get("/products/{id}") {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest, "Неверный ID продукта"
            )

            try {
                val product = ProductDao.getById(id) ?: return@get call.respond(
                    HttpStatusCode.NotFound, "Продукт не найден"
                )

                call.respond(
                    ProductResponse(
                        id = product[Products.id],
                        name = product[Products.name],
                        description = product[Products.description],
                        price = product[Products.price],
                        hasSuppliers = product[Products.hasSuppliers],
                        supplierCount = product[Products.supplierCount]
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, "Ошибка на сервере: ${e.localizedMessage}")
            }
        }

        /**
         * Добавить товар
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
            val params = call.receive<Map<String, String>>()
            val name = params["name"] ?: return@post call.respond("Нет имени")
            val description = params["description"] ?: return@post call.respond("Нет описания")
            val price = params["price"]?.toDoubleOrNull() ?: return@post call.respond("Неверная цена")

            val id = ProductDao.insert(name, description, price)
            call.respond("Продукт создан с ID = $id")
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

        // TODO Обновить данные товара
        put("/products/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull() ?: return@put call.respond(
                    HttpStatusCode.BadRequest,
                    "Некорректный ID"
                )
                val product = call.receive<ProductResponse>()
                ProductDao.update(id, product.name, product.description, product.price)
                call.respond(HttpStatusCode.OK, "Продукт $id обновлен")
            } catch (e: Exception) {
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
                        productName = product?.get(Products.name) ?: "Неизвестный продукт",
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
