package com.example.data

import com.example.*
import com.example.data.dto.dictionaries.*
import com.example.data.dto.order.OrderItemResponse
import com.example.data.dto.product.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.*

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
            val query = Products
                .leftJoin(ProductCategories)
                .leftJoin(ProductCodes)
                .leftJoin(ProductImages)
                .leftJoin(ProductLinks)
                .leftJoin(ProductCounterparties)
                .leftJoin(ProductSuppliers)
                .select(
                    Products.columns + // Все столбцы products
                            ProductCategories.columns +
                            ProductCodes.columns +
                            ProductImages.columns +
                            ProductLinks.columns +
                            ProductCounterparties.columns +
                            ProductSuppliers.columns
                )

            println("SQL запрос выполнен. Получено строк: ${query.count()}")

            query.map { row ->
                println("row keys: ${row.fieldIndex.keys}") // Проверяем доступные ключи
                println("Значение Products.name: ${row[Products.name] ?: "NULL"}")

                val (unitName, unitAbbr) = getMeasurementUnitLocalized(
                    row[Products.measurementUnitId],
                    languageCode = "ru"
                )

                ProductResponse(
                    id = row[Products.id],
                    name = row.getOrNull(Products.name) ?: "Без названия", // Добавляем проверку null
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

                    productCodes = getProductCodes(row[Products.id]),
                    productLinks = getProductLinks(row[Products.id]),
                    productImages = getProductImages(row[Products.id]),
                    productCounterparties = getProductCounterparties(row[Products.id]),
                    productSuppliers = getProductSuppliers(row[Products.id]),
                    productOrderItem = getProductOrders(row[Products.id]),
                    categories = getProductCategories(row[Products.id])
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
            Products.selectAll().where { Products.id eq productId }
                .map {
                    val (unitName, unitAbbr) = getMeasurementUnitLocalized(
                        it[Products.measurementUnitId],
                        languageCode = "ru"
                    )

                    ProductResponse(
                        id = it[Products.id],
                        name = it[Products.name],
                        description = it[Products.description],
                        price = it[Products.price],
                        hasSuppliers = it[Products.hasSuppliers],
                        supplierCount = it[Products.supplierCount],
                        totalStockQuantity = it[Products.totalStockQuantity],
                        minStockQuantity = it[Products.minStockQuantity],
                        isDemanded = it[Products.isDemanded],

                        measurementUnitId = it[Products.measurementUnitId],
                        measurementUnitList = getMeasurementUnit(it[Products.measurementUnitId], "ru"),
                        measurementUnit = unitName,
                        measurementUnitAbbreviation = unitAbbr,

                        productCodes = getProductCodes(productId),
                        productLinks = getProductLinks(productId),
                        productImages = getProductImages(productId),
                        productCounterparties = getProductCounterparties(productId),
                        productSuppliers = getProductSuppliers(productId),
                        productOrderItem = getProductOrders(productId),
                        categories = getProductCategories(productId)
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
     */
    fun insert(product: ProductResponse): Long = transaction {
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
    }

    /**
     * обновление товара (название, цена ....)
     */
    fun update(productId: Long, product: ProductResponse) = transaction {
        Products.update({ Products.id eq productId }) {
            it[name] = product.name
            it[description] = product.description
            it[price] = product.price
            it[hasSuppliers] = product.hasSuppliers
            it[supplierCount] = product.supplierCount
            it[totalStockQuantity] = product.totalStockQuantity
            it[minStockQuantity] = product.minStockQuantity
            it[isDemanded] = product.isDemanded
            it[measurementUnitId] = product.measurementUnitId
        }

        deleteProductDependencies(productId)
        insertProductDependencies(productId, product)
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

    private fun insertProductDependencies(productId: Long, product: ProductResponse) {
        insertProductCodes(productId, product.productCodes)
        insertProductImages(productId, product.productImages)
        insertProductLinks(productId, product.productLinks)
        insertProductCounterparties(productId, product.productCounterparties)
        insertProductSuppliers(productId, product.productSuppliers)
        insertProductCategories(productId, product.categories)
    }

    private fun insertProductCodes(productId: Long, codes: List<ProductCodeResponse>) = transaction {
        ProductCodes.batchInsert(codes) { code ->
            this[ProductCodes.productId] = productId
            this[ProductCodes.code] = code.code
        }
    }

    fun insertProductImages(productId: Long, images: List<ProductImageResponse>) = transaction {
        ProductImages.batchInsert(images) { image ->
            this[ProductImages.productId] = productId
            this[ProductImages.imageBase64] = Base64.getDecoder().decode(image.imageBase64)
        }
    }

    private fun insertProductLinks(productId: Long, links: List<ProductLinkResponse>) = transaction {
        ProductLinks.batchInsert(links) { link ->
            this[ProductLinks.productId] = productId ?: 0L
            this[ProductLinks.counterpartyId] = link.counterpartyId ?: 0L // если null, вставляем 0L
            this[ProductLinks.url] = link.url
        }
    }

    private fun insertProductCounterparties(productId: Long, counterparties: List<ProductCounterpartyResponse>) =
        transaction {
            ProductCounterparties.batchInsert(counterparties) { counterparty ->
                this[ProductCounterparties.productId] = productId
                this[ProductCounterparties.counterpartyId] = counterparty.counterpartyId
                this[ProductCounterparties.stockQuantity] = counterparty.stockQuantity
                this[ProductCounterparties.minStockQuantity] = counterparty.minStockQuantity
                this[ProductCounterparties.measurementUnitId] = counterparty.measurementUnitId
            }
        }

    fun insertProductSuppliers(productId: Long, suppliers: List<ProductSupplierResponse>) = transaction {
        ProductSuppliers.batchInsert(suppliers) { supplier ->
            this[ProductSuppliers.productId] = productId
            this[ProductSuppliers.supplierId] = supplier.supplierId
        }
    }

    private fun insertProductCategories(productId: Long, categories: List<CategoryResponse>) = transaction {
        ProductCategories.batchInsert(categories) { category ->
            this[ProductCategories.productId] = productId
            this[ProductCategories.categoryId] = category.id ?: 0L
        }
    }

    // Получение единицы измерения
    fun getMeasurementUnit(measurementUnitId: Long, languageCode: String = "ru"): MeasurementUnitResponse? = transaction {
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
                    warehouseLocationCodes = it[ProductCounterparties.warehouseLocationCodes]?.split(","),
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

    // Получение категорий товара
    fun getProductCategories(productId: Long): List<CategoryResponse> = transaction {
        val categories = ProductCategories
            .innerJoin(Categories)
            .leftJoin(CategoryTranslations)
            .select(
                Categories.id, Categories.name,
                CategoryTranslations.id, CategoryTranslations.languageCode, CategoryTranslations.name
            )
            .where { ProductCategories.productId eq productId }
            .groupBy { it[Categories.id] }
            .mapNotNull { (categoryId, rows) ->
                val firstRow = rows.firstOrNull() ?: return@mapNotNull null

                val translations = rows.mapNotNull { row ->
                    row[CategoryTranslations.id]?.let {  // Проверяем, есть ли перевод
                        CategoryTranslationResponse(
                            id = it,
                            categoryId = categoryId,
                            languageCode = row[CategoryTranslations.languageCode],
                            name = row[CategoryTranslations.name]
                        )
                    }
                }

                val subcategories = getSubcategories(categoryId)

                CategoryResponse(
                    id = categoryId,
                    name = firstRow[Categories.name],  // Теперь безопасно
                    translations = translations,
                    subcategories = subcategories
                )
            }
        return@transaction categories
    }

    fun getSubcategories(categoryId: Long): List<SubcategoryResponse> = transaction {
        val subcategories = Subcategories
            .leftJoin(SubcategoryTranslations)
            .select(
                Subcategories.id, Subcategories.name, Subcategories.categoryId,
                SubcategoryTranslations.id, SubcategoryTranslations.languageCode, SubcategoryTranslations.name
            )
            .where { Subcategories.categoryId eq categoryId }
            .groupBy { it[Subcategories.id] }
            .mapNotNull { (subcategoryId, rows) ->
                val firstRow = rows.firstOrNull() ?: return@mapNotNull null

                val translations = rows.mapNotNull { row ->
                    row[SubcategoryTranslations.id]?.let {
                        SubcategoryTranslationResponse(
                            id = it,
                            subcategoryId = subcategoryId,
                            languageCode = row[SubcategoryTranslations.languageCode],
                            name = row[SubcategoryTranslations.name]
                        )
                    }
                }

                SubcategoryResponse(
                    id = subcategoryId,
                    name = firstRow[Subcategories.name],
                    categoryId = firstRow[Subcategories.categoryId],
                    translations = translations
                )
            }
        return@transaction subcategories
    }

    // Получение кодов товара
    fun getProductCodes(productId: Long): List<ProductCodeResponse> = transaction {
        ProductCodes.selectAll().where { ProductCodes.productId eq productId }
            .map {
                ProductCodeResponse(
                    productId = it[ProductCodes.productId],
                    code = it[ProductCodes.code]
                )
            }
    }

    // Получение ссылок на товар
    fun getProductLinks(productId: Long): List<ProductLinkResponse> = transaction {
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

    // Получение изображений товара
    fun getProductImages(productId: Long): List<ProductImageResponse> = transaction {
        ProductImages.selectAll().where { ProductImages.productId eq productId }
            .map {
                ProductImageResponse(
                    id = it[ProductImages.id],
                    productId = it[ProductImages.productId],
                    imageBase64 = Base64.getEncoder()
                        .encodeToString(it[ProductImages.imageBase64]) // Преобразуем бинарные данные в Base64
                )
            }
    }
}