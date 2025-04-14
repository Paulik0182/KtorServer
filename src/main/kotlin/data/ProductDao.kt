package com.example.data

import com.example.*
import com.example.data.CategoryDao.getCategoryIds
import com.example.data.CategoryDao.getProductCategories
import com.example.data.CategoryDao.getProductSubcategories
import com.example.data.CategoryDao.getSubcategoryIds
import com.example.data.CategoryDao.insertProductSubcategories
import com.example.data.ProductImageDao.getProductImages
import com.example.data.ProductImageDao.insertProductImagePath
import com.example.data.dto.dictionaries.*
import com.example.data.dto.order.OrderItemResponse
import com.example.data.dto.product.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

/**
 * Этот объект управляет товарами (products) в базе данных.
 * Он позволяет получать список товаров, добавлять новые, удалять и получать информацию по ID.
 */
object ProductDao {

    /**
     * Получение всех товаров
     */
    fun getAll(): List<ProductResponse> = transaction {
        try {
            val productRows = Products.selectAll().toList()
            println("Получено продуктов: ${productRows.size}")

            productRows.map { row ->
                val productId = row[Products.id]

                val (unitName, unitAbbr) = getMeasurementUnitLocalized(
                    row[Products.measurementUnitId],
                    languageCode = "ru"
                )

                val currencyRow =
                    Currencies.selectAll().where { Currencies.id eq row[Products.currencyId] }.firstOrNull()
                val currencyCode = currencyRow?.get(Currencies.code)
                val currencySymbol = currencyRow?.get(Currencies.symbol)
                val currencyName = currencyRow?.get(Currencies.name)
                val currencyId = currencyRow?.get(Currencies.id)

                ProductResponse(
                    id = productId,
                    name = row[Products.name],
                    description = row[Products.description],
                    price = row[Products.price],
                    hasSuppliers = row[Products.hasSuppliers],
                    supplierCount = row[Products.supplierCount],
                    totalStockQuantity = row[Products.totalStockQuantity],
                    minStockQuantity = row[Products.minStockQuantity],
                    isDemanded = row[Products.isDemanded],
                    measurementUnitId = row[Products.measurementUnitId],
                    measurementUnitList = getMeasurementUnit(row[Products.measurementUnitId], "ru"),
                    measurementUnit = unitName,
                    measurementUnitAbbreviation = unitAbbr,

                    productCodes = getProductCodes(productId),
                    productLinks = getProductLinks(productId),
                    productImages = getProductImages(productId),
                    productCounterparties = getProductCounterparties(productId),
                    productSuppliers = getProductSuppliers(productId),
                    productOrderItem = getProductOrders(productId),
                    categories = getProductCategories(productId),
                    categoryIds = getCategoryIds(productId),
                    subcategoryIds = getSubcategoryIds(productId),
                    subcategories = getProductSubcategories(productId),
                    currencyCode = currencyCode,
                    currencySymbol = currencySymbol,
                    currencyName = currencyName,
                    currencyId = currencyId
                )
            }
        } catch (e: Exception) {
            println("Ошибка в getAll(): ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Получение товара по ID
     */
    fun getById(productId: Long): ProductResponse? = transaction {
        try {
            val row = Products
                .selectAll()
                .where { Products.id eq productId }
                .firstOrNull() ?: return@transaction null

            val (unitName, unitAbbr) = getMeasurementUnitLocalized(
                row[Products.measurementUnitId],
                languageCode = "ru"
            )

            val currencyRow = Currencies.selectAll().where { Currencies.id eq row[Products.currencyId] }.firstOrNull()
            val currencyCode = currencyRow?.get(Currencies.code)
            val currencySymbol = currencyRow?.get(Currencies.symbol)
            val currencyName = currencyRow?.get(Currencies.name)
            val currencyId = currencyRow?.get(Currencies.id)

            ProductResponse(
                id = row[Products.id],
                name = row[Products.name] ?: "Без названия",
                description = row[Products.description] ?: "",
                price = row[Products.price] ?: BigDecimal.ZERO,
                hasSuppliers = row[Products.hasSuppliers] ?: false,
                supplierCount = row[Products.supplierCount] ?: 0,
                totalStockQuantity = row[Products.totalStockQuantity] ?: 0,
                minStockQuantity = row[Products.minStockQuantity] ?: 0,
                isDemanded = row[Products.isDemanded] ?: true,

                measurementUnitId = row[Products.measurementUnitId],
                measurementUnitList = getMeasurementUnit(row[Products.measurementUnitId], "ru"),
                measurementUnit = unitName,
                measurementUnitAbbreviation = unitAbbr,

                productCodes = getProductCodes(productId),
                productLinks = getProductLinks(productId),
                productImages = getProductImages(productId),
                productCounterparties = getProductCounterparties(productId),
                productSuppliers = getProductSuppliers(productId),
                productOrderItem = getProductOrders(productId),
                categories = getProductCategories(productId),
                categoryIds = getCategoryIds(productId),
                subcategoryIds = getSubcategoryIds(productId),
                subcategories = getProductSubcategories(productId),
                currencyCode = currencyCode,
                currencySymbol = currencySymbol,
                currencyName = currencyName,
                currencyId = currencyId
            )
        } catch (e: Exception) {
            println("Ошибка в getById($id): ${e.localizedMessage}")
            throw e
        }
    }

    /**
     * Добавление нового товара
     */
    fun insert(product: ProductCreateRequest): Long = transaction {
        // Проверяем, существует ли уже продукт с таким названием TODO Это решение более целенаправленное и лучше использовать вот эту проверку.
        val existingProductId = Products
            .selectAll().where { Products.name eq product.name }
            .map { it[Products.id] }
            .firstOrNull()

        // Если нашли — возвращаем его ID (или можешь выбросить ошибку, если нужно)
        if (existingProductId != null) {
            throw DuplicateProductNameException(product.name, existingProductId)
        }

        // Вставляем новый продукт
        val productId = Products.insertReturning(listOf(Products.id)) {
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[hasSuppliers] = product.hasSuppliers
            it[supplierCount] = product.supplierCount
            it[totalStockQuantity] = product.totalStockQuantity
            it[minStockQuantity] = product.minStockQuantity
            it[isDemanded] = product.isDemanded
            it[measurementUnitId] = product.measurementUnitId
            it[currencyId] = product.currencyId
        }.single()[Products.id]

        insertProductDependencies(productId, product)
        return@transaction productId
    }

    /**
     * Удаление товара и всех зависимостей
     */
    fun delete(productId: Long) = transaction {
        deleteProductDependencies(productId)
        Products.deleteWhere { Products.id eq productId }
    }

    private fun deleteProductDependencies(productId: Long) {
        ProductCodes.deleteWhere { ProductCodes.productId eq productId }
        ProductImages.deleteWhere { ProductImages.productId eq productId }
        ProductLinks.deleteWhere { ProductLinks.productId eq productId }
        ProductCounterparties.deleteWhere { ProductCounterparties.productId eq productId }
        ProductSuppliers.deleteWhere { ProductSuppliers.productId eq productId }
        ProductCategories.deleteWhere { ProductCategories.productId eq productId }
        ProductSubcategories.deleteWhere { ProductSubcategories.productId eq productId }
    }

    /**
     * обновление товара (название, цена ....)
     */
    fun update(productId: Long, product: ProductCreateRequest) = transaction {
        // Получаем текущий товар по ID
        val currentProduct = Products
            .selectAll().where { Products.id eq productId }
            .firstOrNull()
            ?: error(
                HttpStatusCode.NotFound,
                mapOf("error" to "not_found", "message" to "Продукт с ID $productId не найден")
            )

        val currentName = currentProduct[Products.name]

        // Если имя изменилось — проверяем на дубликат
        if (currentName != product.name) {
            val duplicateProduct = Products
                .selectAll().where { Products.name eq product.name }
                .firstOrNull()

            if (duplicateProduct != null) {
                error(
                    HttpStatusCode.Conflict,
                    mapOf(
                        "error" to "duplicate_name",
                        "message" to "Товар с названием '${product.name}' уже существует (ID:${duplicateProduct[Products.id]})"
                    )
                )
            }
        }

        // Обновляем основную запись
        val updatedCount = Products.update({ Products.id eq productId }) {
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[hasSuppliers] = product.hasSuppliers
            it[supplierCount] = product.supplierCount
            it[totalStockQuantity] = product.totalStockQuantity
            it[minStockQuantity] = product.minStockQuantity
            it[isDemanded] = product.isDemanded
            it[measurementUnitId] = product.measurementUnitId
            it[currencyId] = product.currencyId
        }

        if (updatedCount == 0) {
            error(
                HttpStatusCode.NotFound,
                mapOf("error" to "not_updated", "message" to "Не удалось обновить продукт с ID $productId")
            )
        }

        deleteProductDependencies(productId)
        insertProductDependencies(productId, product)
    }

    fun getAllCurrencies(): List<CurrencyResponse> = transaction {
        Currencies.selectAll().map {
            CurrencyResponse(
                id = it[Currencies.id],
                code = it[Currencies.code],
                symbol = it[Currencies.symbol],
                name = it[Currencies.name]
            )
        }
    }

    /**
     * Получить поставщиков товара
     */
    fun getSuppliersByProduct(productId: Long): List<ResultRow> = transaction {
        ProductSuppliers
            .innerJoin(Counterparties)
            .selectAll().where { ProductSuppliers.productId eq productId }
            .toList()
    }

    private fun insertProductDependencies(productId: Long, product: ProductCreateRequest) {
        insertProductCodes(productId, product.productCodes)
        product.productImages.forEach { image ->
            insertProductImagePath(productId, image.fileName)
        }

        insertProductLinks(productId, product.productLinks)
        insertProductCounterparties(productId, product.productCounterparties)
        insertProductSuppliers(productId, product.productSuppliers)
        insertProductCategories(productId, product.categories)
        insertProductSubcategories(productId, product.subcategories)
    }

    private fun insertProductCodes(productId: Long, codes: List<ProductCodeRequest>) = transaction {
        codes.forEach { code ->
            // Проверка на существование кода
            val existingCodeId = Codes
                .selectAll()
                .where { Codes.code eq code.code }
                .map { it[Codes.id] }
                .firstOrNull()

            // Если код не найден, добавляем
            val codeId = existingCodeId ?: (Codes.insert {
                it[Codes.code] = code.code
            } get Codes.id)

            // Вставляем связь product <-> code
            ProductCodes.insert {
                it[ProductCodes.productId] = productId
                it[ProductCodes.codeId] = codeId
            }
        }
    }

    private fun insertProductLinks(productId: Long, links: List<ProductLinkRequest>) = transaction {
        links.forEach { link ->
            // Ищем существующий URL
            val existingUrlId = Urls
                .selectAll().where { Urls.url eq link.url }
                .map { it[Urls.id] }
                .firstOrNull()

            // Если URL не найден, добавляем его
            val urlId = existingUrlId ?: (Urls.insert {
                it[Urls.url] = link.url
            } get Urls.id) // Получаем id вставленной строки

            // Вставляем связь product <-> url
            ProductLinks.insert {
                it[ProductLinks.productId] = productId
//                it[ProductLinks.counterpartyId] = link.counterpartyId ?: 0L
                it[ProductLinks.urlId] = urlId
            }
        }
    }

    private fun insertProductCounterparties(productId: Long, counterparties: List<ProductCounterpartyRequest>) =
        transaction {
            ProductCounterparties.batchInsert(counterparties) { counterparty ->
                this[ProductCounterparties.productId] = productId
                this[ProductCounterparties.counterpartyId] = counterparty.counterpartyId
                this[ProductCounterparties.stockQuantity] = counterparty.stockQuantity
                this[ProductCounterparties.role] = counterparty.role
                this[ProductCounterparties.minStockQuantity] = counterparty.minStockQuantity
                this[ProductCounterparties.warehouseLocationCodes] = counterparty.warehouseLocationCodes
                this[ProductCounterparties.measurementUnitId] = counterparty.measurementUnitId
            }
        }

    fun insertProductSuppliers(productId: Long, suppliers: List<ProductSupplierRequest>) = transaction {
        ProductSuppliers.batchInsert(suppliers) { supplier ->
            this[ProductSuppliers.productId] = productId
            this[ProductSuppliers.supplierId] = supplier.supplierId
        }
    }

    private fun insertProductCategories(productId: Long, categoryIds: List<Long>) = transaction {
        ProductCategories.batchInsert(categoryIds) { categoryId ->
            this[ProductCategories.productId] = productId
            this[ProductCategories.categoryId] = categoryId
        }
    }

    // Получение единицы измерения
    fun getMeasurementUnit(measurementUnitId: Long, languageCode: String = "ru"): MeasurementUnitResponse? =
        transaction {
            val row = MeasurementUnits
                .leftJoin(MeasurementUnitTranslations)
                .selectAll().where {
                    (MeasurementUnits.id eq measurementUnitId) and
                            (MeasurementUnitTranslations.languageCode eq languageCode)
                }
                .limit(1)
                .firstOrNull()

            if (row != null) {
                val name = row[MeasurementUnitTranslations.name]
                val abbreviation = row[MeasurementUnitTranslations.abbreviation]
                return@transaction MeasurementUnitResponse(
                    id = row[MeasurementUnits.id],
                    name = name,
                    abbreviation = abbreviation ?: row[MeasurementUnits.abbreviation] ?: "",
                    translations = listOf(
                        MeasurementUnitTranslationResponse(
                            id = row[MeasurementUnitTranslations.id],
                            measurementUnitId = measurementUnitId,
                            languageCode = languageCode,
                            name = name,
                            abbreviation = abbreviation
                        )
                    )
                )
            }

            // Если перевода нет — берём только из основной таблицы
            val fallback = MeasurementUnits
                .selectAll().where { MeasurementUnits.id eq measurementUnitId }
                .firstOrNull()

            fallback?.let {
                MeasurementUnitResponse(
                    id = it[MeasurementUnits.id],
                    name = it[MeasurementUnits.name],
                    abbreviation = it[MeasurementUnits.abbreviation] ?: "",
                    translations = emptyList()
                )
            }
        }

    fun getMeasurementUnitLocalized(id: Long, languageCode: String = "ru"): Pair<String?, String?> {
        val result = MeasurementUnits
            .leftJoin(MeasurementUnitTranslations)
            .selectAll().where {
                (MeasurementUnits.id eq id) and (MeasurementUnitTranslations.languageCode eq languageCode)
            }
            .limit(1)
            .firstOrNull()

        return if (result != null) {
            result[MeasurementUnitTranslations.name] to (result[MeasurementUnitTranslations.abbreviation] ?: "")
        } else {
            // Если перевода нет — вернем из основной таблицы
            val fallback = MeasurementUnits.selectAll().where { MeasurementUnits.id eq id }.firstOrNull()
            fallback?.get(MeasurementUnits.name) to (fallback?.get(MeasurementUnits.abbreviation) ?: "")
        }
    }

    // Получение складов, где хранится товар
    fun getProductCounterparties(productId: Long): List<ProductCounterpartyResponse> = transaction {

        ProductCounterparties.selectAll().where { ProductCounterparties.productId eq productId }
            .map {
                val unit = getMeasurementUnit(it[ProductCounterparties.measurementUnitId], "ru")

                val (unitName, unitAbbr) = getMeasurementUnitLocalized(
                    it[ProductCounterparties.measurementUnitId],
                    languageCode = "ru"
                )

                ProductCounterpartyResponse(
                    productId = it[ProductCounterparties.productId],
                    productName = getProductName(it[ProductCounterparties.productId]),
                    counterpartyId = it[ProductCounterparties.counterpartyId],
                    counterpartyName = getCounterpartyName(it[ProductCounterparties.counterpartyId]),
                    stockQuantity = it[ProductCounterparties.stockQuantity],
                    role = it[ProductCounterparties.role],
                    minStockQuantity = it[ProductCounterparties.minStockQuantity],
                    warehouseLocationCodes = it[ProductCounterparties.warehouseLocationCodes],
                    measurementUnitId = it[ProductCounterparties.measurementUnitId],
                    measurementUnitList = unit,
                    measurementUnit = unitName,
                    measurementUnitAbbreviation = unitAbbr,
                )
            }
    }

    // Получение поставщиков товара
    fun getProductSuppliers(productId: Long): List<ProductSupplierResponse> = transaction {
        ProductSuppliers
            .innerJoin(Counterparties)
            .selectAll().where { ProductSuppliers.productId eq productId }
            .map {
                ProductSupplierResponse(
                    id = it[ProductSuppliers.id],
                    productId = it[ProductSuppliers.productId],
                    productName = getProductName(it[ProductSuppliers.productId]),
                    supplierId = it[ProductSuppliers.supplierId],
                    supplierName = getCounterpartyName(it[ProductSuppliers.supplierId])
                )
            }
    }

    // Получение заказчиков товара
    fun getProductOrders(productId: Long): List<OrderItemResponse> = transaction {
        OrderItems
            .innerJoin(Orders)
            .innerJoin(Counterparties)
            .selectAll().where { OrderItems.productId eq productId }
            .map {
                val (unitName, unitAbbr) = getMeasurementUnitLocalized(
                    it[OrderItems.measurementUnitId],
                    languageCode = "ru"
                )

                OrderItemResponse(
                    id = it[OrderItems.id],
                    orderId = it[OrderItems.orderId],
                    productId = it[OrderItems.productId],
                    productName = getProductName(it[OrderItems.productId]),
                    quantity = it[OrderItems.quantity],
                    measurementUnitId = it[OrderItems.measurementUnitId],
                    measurementUnitList = getMeasurementUnit(it[OrderItems.measurementUnitId], "ru"),
                    measurementUnit = unitName,
                    measurementUnitAbbreviation = unitAbbr
                )
            }
    }

    fun getProductName(id: Long): String = transaction {
        Products.selectAll().where { Products.id eq id }
            .map { it[Products.name] }
            .singleOrNull() ?: "Неизвестный продукт"
    }

    fun getCounterpartyName(id: Long): String = transaction {
        Counterparties.selectAll().where { Counterparties.id eq id }
            .map { it[Counterparties.name] }
            .singleOrNull() ?: "Нет названия фирмы"
    }

    // Получение кодов товара
    fun getProductCodes(productId: Long): List<ProductCodeResponse> = transaction {
        (ProductCodes innerJoin Codes).selectAll().where {
            ProductCodes.productId eq productId
        }.map {
            val codId = it[ProductCodes.codeId]
            val codText = it[Codes.code]

            ProductCodeResponse(
                productId = it[ProductCodes.productId],
                codId = codId,
                codeName = codText,
                code = listOf(CodesResponse(id = codId, code = codText))
            )
        }
    }

    // Получение ссылок на товар
    fun getProductLinks(productId: Long): List<ProductLinkResponse> = transaction {
        (ProductLinks innerJoin Urls).selectAll().where {
            ProductLinks.productId eq productId
        }.map {
            val urlId = it[ProductLinks.urlId]
            val urlText = it[Urls.url]

            ProductLinkResponse(
                id = it[ProductLinks.id],
                productId = it[ProductLinks.productId],
                counterpartyId = it[ProductLinks.counterpartyId],
                urlId = urlId,
                urlName = urlText,
                url = listOf(UrlsResponse(id = urlId, url = urlText))
            )
        }
    }
}