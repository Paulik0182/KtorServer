package com.example.data

import com.example.*
import com.example.data.dto.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.math.BigDecimal
import java.util.Base64

/**
 * Этот объект управляет товарами (products) в базе данных.
 * Он позволяет получать список товаров, добавлять новые, удалять и получать информацию по ID.
 */
object ProductDao {

    /**
     * Получение всех товаров
     *
     * Метод не принимает аргументов.
     * Возвращает список строк (ResultRow) из таблицы products.
     * = transaction { Открываем транзакцию.
     * Products.selectAll()
     *
     * Выполняем SQL-запрос SELECT * FROM products.
     * Получаем все записи из таблицы products.
     * .toList()
     *
     * Преобразуем результат в список List<ResultRow>.
     * Как использовать?
     * val products = ProductDao.getAll()
     * Получим все товары в виде списка.
     */
    fun getAll2(): List<ResultRow> = transaction {
        Products.selectAll().toList()
    }

    fun getAll(): List<ProductResponse> = transaction {
        try {
            Products.leftJoin(ProductLinks).leftJoin(ProductLocations).leftJoin(ProductImages)
                .selectAll()
                .groupBy { it[Products.id] }
                .map { (productId, rows) ->
                    val firstRow = rows.first()
                    ProductResponse(
                        id = firstRow[Products.id],
                        name = firstRow[Products.name],
                        description = firstRow[Products.description],
                        price = firstRow[Products.price],
                        hasSuppliers = firstRow[Products.hasSuppliers],
                        supplierCount = firstRow[Products.supplierCount],
                        stockQuantity = firstRow[Products.stockQuantity],
                        minStockQuantity = firstRow[Products.minStockQuantity],
                        productCodes = getProductCodes(productId).map { it.code },
                        isDemanded = firstRow[Products.isDemanded],
                        productLinks = getProductLinks(productId),
                        locations = getProductLocations(productId),
                        images = getProductImages(productId),
                        categories = try {
                            Json.decodeFromString(firstRow[Products.categories] ?: "[]")
                        } catch (e: Exception) {
                            println("Ошибка десериализации categories: ${e.localizedMessage}")
                            emptyList()
                        },
                        subcategories = try {
                            Json.decodeFromString(firstRow[Products.subcategories] ?: "[]")
                        } catch (e: Exception) {
                            println("Ошибка десериализации subcategories: ${e.localizedMessage}")
                            emptyList()
                        }
                    )
                }
        } catch (e: Exception) {
            println("Ошибка в getAll(): ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Получение товара по ID
     *
     * Метод принимает id: Int – ID товара.
     * Возвращает одну строку (ResultRow) или null, если товара нет.
     * = transaction { Открываем транзакцию.
     * Products.select { Products.id eq id }
     *
     * Выполняем SQL-запрос:
     * SELECT * FROM products WHERE id = ?
     * .singleOrNull()
     *
     * Если товар найден – вернем одну запись.
     * Если товара нет – вернем null.
     * Метод:
     * Ищет товар по ID.
     * Если товара нет – возвращает null.
     *
     * Как использовать?
     * val product = ProductDao.getById(5)
     * Найдем товар с ID = 5.
     */
    fun getById2(id: Int): ResultRow? = transaction {
        Products
            .selectAll().where { Products.id eq id }
            .singleOrNull()
    }

    fun getById(id: Int): ProductResponse? = transaction {
        try {
            Products.selectAll().where { Products.id eq id }
                .map {
                    ProductResponse(
                        id = it[Products.id],
                        name = it[Products.name],
                        description = it[Products.description],
                        price = it[Products.price],
                        hasSuppliers = it[Products.hasSuppliers],
                        supplierCount = it[Products.supplierCount],
                        stockQuantity = it[Products.stockQuantity],
                        minStockQuantity = it[Products.minStockQuantity],
                        categories = try {
                            Json.decodeFromString(it[Products.categories] ?: "[]")
                        } catch (e: Exception) {
                            println("Ошибка десериализации categories: ${e.localizedMessage}")
                            emptyList()
                        },
                        subcategories = try {
                            Json.decodeFromString(it[Products.subcategories] ?: "[]")
                        } catch (e: Exception) {
                            println("Ошибка десериализации subcategories: ${e.localizedMessage}")
                            emptyList()
                        },
                        productCodes = getProductCodes(id).map { it.code },
                        isDemanded = it[Products.isDemanded],
                        productLinks = getProductLinks(id),
                        locations = getProductLocations(id),
                        images = getProductImages(id)
                    )
                }
                .singleOrNull()
        } catch (e: Exception) {
            println("Ошибка в getById($id): ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Добавление нового товара
     *
     * Метод принимает:
     * name: String → название товара.
     * description: String → описание товара.
     * price: Double → цена товара.
     * = transaction { Открываем транзакцию.
     * Products.insert {
     *
     * Выполняем SQL-запрос INSERT INTO products.
     * it[Products.name] = name
     *
     * Вставляем название товара.
     * it[Products.description] = description
     *
     * Вставляем описание.
     * it[Products.price] = price.toBigDecimal()
     *
     * Конвертируем Double в BigDecimal, чтобы сохранить в БД.
     * } get Products.id
     *
     * После вставки возвращаем ID новой записи.
     * Метод:
     * Добавляет новый товар в базу.
     * Возвращает его ID.
     *
     * Как использовать?
     * val newProductId = ProductDao.insert("Телефон", "Смартфон", 599.99)
     * Добавляем товар "Телефон", "Смартфон", цена 599.99.
     * Метод вернет ID нового товара.
     */
    fun insert(
        name: String,
        description: String,
        price: BigDecimal,
        stockQuantity: Int,
        minStockQuantity: Int,
        productCodes: List<String>,
        isDemanded: Boolean,
        productLinks: List<ProductLinkResponse>,
        locations: List<WarehouseLocationResponse>,
        images: List<ProductImageResponse>,
        categories: List<String>,
        subcategories: List<String>
    ): Int = transaction {
        val productId = Products.insertReturning(listOf(Products.id)) {
            it[Products.name] = name
            it[Products.description] = description
            it[Products.price] = price
            it[Products.stockQuantity] = stockQuantity
            it[Products.minStockQuantity] = minStockQuantity
            it[Products.isDemanded] = isDemanded
            it[Products.categories] = Json.encodeToString(categories)
            it[Products.subcategories] = Json.encodeToString(subcategories)
        }.single()[Products.id]

        insertProductCodes(productId, productCodes)
        insertProductLinks(productId, productLinks)
        insertProductLocations(productId, locations)
        insertProductImages(productId, images)

        productId
    }

    /**
     * Удаление товара
     *
     * Метод принимает id: Int – ID товара для удаления.
     * Products.deleteWhere { Products.id eq id }
     * Выполняем SQL-запрос:
     * DELETE FROM products WHERE id = ?
     * Удаляем товар по ID.
     * Как использовать?
     * ProductDao.delete(3)
     * Удалим товар с ID = 3.
     */
    fun delete(id: Int) = transaction {
        Products.deleteWhere { Products.id eq id }
    }

    /**
     * Удаление товара с очисткой связей
     *
     * Метод принимает id: Int – ID товара для удаления.
     * ProductSuppliers.deleteWhere { ProductSuppliers.productId eq id }
     *
     * Сначала удаляем связи товара с поставщиками.
     * Products.deleteWhere { Products.id eq id }
     *
     * Затем удаляем сам товар.
     * Метод:
     * Удаляет товар и очищает его связи с поставщиками.
     *
     * Как использовать?
     * ProductDao.delete2(4)
     * Удалит товар с ID = 4.
     * Очистит все связи с поставщиками.
     */
    fun deleteWithSuppliers(id: Int) = transaction {
        // Удаляем связь товара с поставщиками
        ProductSuppliers.deleteWhere { ProductSuppliers.productId eq id }
        Products.deleteWhere { Products.id eq id }
    }

    /**
     * обновление товара (название, цена ....)
     *
     * Метод принимает ID товара и новые значения (название, цена ...).
     * Обновляет товар в базе данных.
     * transaction { Открываем транзакцию, чтобы все изменения были атомарными.
     * Products.update({ Products.id eq id }) {
     *
     * Выполняем SQL-запрос UPDATE products SET ... WHERE id = ?.
     * it[Products.name] = name
     *
     * Устанавливаем новое название.
     * it[Products.description] = description
     *
     * Устанавливаем новое описание.
     * it[Products.price] = price.toBigDecimal()
     *
     * Конвертируем Double → BigDecimal, чтобы сохранить в БД.
     * Как использовать метод update()?
     * ProductDao.update(2, "Ноутбук", "Игровой ноутбук с RTX 4080", 2999.99)
     * Найдет товар с ID = 2.
     * Изменит его название на "Ноутбук".
     * Обновит описание на "Игровой ноутбук с RTX 4080".
     * Поставит новую цену 2999.99.
     */
    fun update(
        id: Int,
        name: String,
        description: String,
        price: BigDecimal,
        stockQuantity: Int,
        minStockQuantity: Int,
        productCodes: List<String>,
        isDemanded: Boolean,
        productLinks: List<ProductLinkResponse>,
        locations: List<WarehouseLocationResponse>,
        images: List<ProductImageResponse>,
        categories: List<String>,
        subcategories: List<String>
    ) = transaction {
        Products.update({ Products.id eq id }) {
            it[Products.name] = name
            it[Products.description] = description
            it[Products.price] = price
            it[Products.stockQuantity] = stockQuantity
            it[Products.minStockQuantity] = minStockQuantity
            it[Products.isDemanded] = isDemanded
            it[Products.categories] = Json.encodeToString(categories).toString()
            it[Products.subcategories] = Json.encodeToString(subcategories).toString()
        }

        deleteProductCodes(id)
        insertProductCodes(id, productCodes)

        deleteProductLinks(id)
        insertProductLinks(id, productLinks)

        deleteProductLocations(id)
        insertProductLocations(id, locations)

        deleteProductImages(id)
        insertProductImages(id, images)
    }

    /**
     * Получить поставщиков товара
     *
     * Метод принимает ID товара (productId).
     * Возвращает список поставщиков (List<ResultRow>).
     * = transaction { Открываем транзакцию, чтобы гарантировать атомарность.
     * ProductSuppliers.innerJoin(Counterparties)
     *
     * Объединяем таблицы product_suppliers и counterparties по supplier_id.
     * Это аналог SQL-запроса:
     * SELECT * FROM product_suppliers
     * INNER JOIN counterparties ON product_suppliers.supplier_id = counterparties.id
     * WHERE product_suppliers.product_id = ?;
     * .select { ProductSuppliers.productId eq productId }
     *
     * Выбираем только те записи, которые относятся к переданному productId.
     * .toList()
     *
     * Преобразуем результат в список поставщиков (List<ResultRow>).
     * Как использовать метод getSuppliersByProduct()?
     * val suppliers = ProductDao.getSuppliersByProduct(5)
     * Найдет всех поставщиков товара с ID = 5.
     * Вернет список записей из counterparties.
     */
    fun getSuppliersByProduct(productId: Int): List<ResultRow> = transaction {
        ProductSuppliers
            .innerJoin(Counterparties)
            .selectAll().where { ProductSuppliers.productId eq productId }
            .toList()
    }

    fun getProductCodes(productId: Int): List<ProductCodeResponse> = transaction {
        ProductCodes.selectAll().where { ProductCodes.productId eq productId }
            .map {
                ProductCodeResponse(
                    productId = it[ProductCodes.productId],
                    code = it[ProductCodes.code]
                )
            }
    }

    fun getProductLinks(productId: Int): List<ProductLinkResponse> = transaction {
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

    fun getProductLocations(productId: Int): List<WarehouseLocationResponse> = transaction {
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

    fun getProductImages(productId: Int): List<ProductImageResponse> = transaction {
        ProductImages.selectAll().where { ProductImages.productId eq productId }
            .map {
                ProductImageResponse(
                    id = it[ProductImages.id],
                    productId = it[ProductImages.productId],
                    imageBase64 = Base64.getEncoder()
                        .encodeToString(it[ProductImages.image]) // Преобразуем бинарные данные в Base64
                )
            }
    }

    fun insertProductCodes(productId: Int, codes: List<String>) = transaction {
        ProductCodes.batchInsert(codes) { code ->
            this[ProductCodes.productId] = productId
            this[ProductCodes.code] = code
        }
    }

    fun insertProductLinks(productId: Int, links: List<ProductLinkResponse>) = transaction {
        ProductLinks.batchInsert(links) { link ->
            this[ProductLinks.productId] = productId
            this[ProductLinks.counterpartyId] = link.counterpartyId
            this[ProductLinks.url] = link.url
        }
    }

    fun insertProductLocations(productId: Int, locations: List<WarehouseLocationResponse>) = transaction {
        ProductLocations.batchInsert(locations) { location ->
            this[ProductLocations.productId] = productId
            this[ProductLocations.locationId] = location.id
        }
    }

    fun insertProductImages(productId: Int, images: List<ProductImageResponse>) = transaction {
        ProductImages.batchInsert(images) { image ->
            this[ProductImages.productId] = productId
            this[ProductImages.image] =
                Base64.getDecoder().decode(image.imageBase64) // Преобразуем Base64 обратно в ByteArray
        }
    }

    fun deleteProductCodes(productId: Int) = transaction {
        ProductCodes.deleteWhere { ProductCodes.productId eq productId }
    }

    fun deleteProductLinks(productId: Int) = transaction {
        ProductLinks.deleteWhere { ProductLinks.productId eq productId }
    }

    fun deleteProductLocations(productId: Int) = transaction {
        ProductLocations.deleteWhere { ProductLocations.productId eq productId }
    }

    fun deleteProductImages(productId: Int) = transaction {
        ProductImages.deleteWhere { ProductImages.productId eq productId }
    }
}