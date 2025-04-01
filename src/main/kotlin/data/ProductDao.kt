package com.example.data

import com.example.*
import com.example.data.dto.dictionaries.*
import com.example.data.dto.order.OrderItemResponse
import com.example.data.dto.product.*
import io.ktor.http.*
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
                    categories = getProductCategories(row[Products.id]),
                    categoryIds = getCategoryIds(row[Products.id]),
                    subcategoryIds = getSubcategoryIds(row[Products.id]),
                    subcategories = getProductSubcategories(row[Products.id])
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
                        categories = getProductCategories(productId),
                        categoryIds = getCategoryIds(it[Products.id]),
                        subcategoryIds = getSubcategoryIds(it[Products.id]),
                        subcategories = getProductSubcategories(productId)
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
        insertProductImages(productId, product.productImages)
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

    fun insertProductImages(productId: Long, images: List<ProductImageRequest>) = transaction {
        ProductImages.batchInsert(images.filter { it.imageBase64.isNotBlank() }) { image ->
            try {
                val decoded = Base64.getDecoder().decode(image.imageBase64)
                this[ProductImages.productId] = productId
                this[ProductImages.imageBase64] = decoded
            } catch (e: IllegalArgumentException) {
                error("Ошибка декодирования base64: ${image.imageBase64}")
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
                it[ProductLinks.counterpartyId] = link.counterpartyId ?: 0L
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

    // Получение категорий товара
    fun getProductCategories(productId: Long): List<CategoryResponse> = transaction {

        // Получаем ID подкатегорий, которые связаны с этим товаром
        val selectedSubcategoryIds = getSubcategoryIds(productId).toSet()

        // Выбираем все категории, к которым привязан продукт
        val categoryRows = ProductCategories
            .innerJoin(Categories)
            .leftJoin(CategoryTranslations)
            .selectAll()
            .where { ProductCategories.productId eq productId }

        // Группируем по ID категории
        val groupedByCategory = categoryRows.groupBy { it[Categories.id] }

        return@transaction groupedByCategory.mapNotNull { (categoryId, rows) ->
            val firstRow = rows.firstOrNull() ?: return@mapNotNull null

            // Переводы
            val translations = rows.mapNotNull { row ->
                row.getOrNull(CategoryTranslations.id)?.let {
                    CategoryTranslationResponse(
                        id = it,
                        categoryId = categoryId,
                        languageCode = row[CategoryTranslations.languageCode],
                        name = row[CategoryTranslations.name]
                    )
                }
            }

            // Подкатегории этой категории
            val allSubcategories = getSubcategories(categoryId)

            // Фильтруем только те, что реально выбраны у этого товара
            val selectedSubcategories = allSubcategories.filter { it.id in selectedSubcategoryIds }

            // Собираем результат
            CategoryResponse(
                id = categoryId,
                name = firstRow[Categories.name],
                translations = translations,
                subcategories = selectedSubcategories,
                imageUrl = firstRow.getOrNull(Categories.imageUrl)
                    ?.let { "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(it) }
            )
        }
    }

    fun getSubcategories(categoryId: Long): List<SubcategoryResponse> = transaction {
        val subcategories = Subcategories
            .leftJoin(SubcategoryTranslations)
            .select(
                Subcategories.id,
                Subcategories.name,
                Subcategories.categoryId,
                Subcategories.imageUrl,
                SubcategoryTranslations.id,
                SubcategoryTranslations.languageCode,
                SubcategoryTranslations.name
            )
            .where { Subcategories.categoryId eq categoryId }
            .groupBy { it[Subcategories.id] }
            .mapNotNull { (subcategoryId, rows) ->
                val firstRow = rows.firstOrNull() ?: return@mapNotNull null

                val fallbackName = firstRow[Subcategories.name]
                val fallbackCategoryId = firstRow[Subcategories.categoryId]
                val fallbackImage = firstRow.getOrNull(Subcategories.imageUrl)

                val translations = rows.mapNotNull { row ->
                    row.getOrNull(SubcategoryTranslations.id)?.let {
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
                    name = fallbackName,
                    categoryId = fallbackCategoryId,
                    translations = translations,
                    imageUrl = fallbackImage?.let {
                        "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(it)
                    }
                )
            }

        return@transaction subcategories
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

    fun getCategoryIds(productId: Long): List<Long> = transaction {
        ProductCategories
            .selectAll().where { ProductCategories.productId eq productId }
            .map { it[ProductCategories.categoryId] }
    }

    fun getSubcategoryIds(productId: Long): List<Long> = transaction {
        ProductSubcategories
            .selectAll().where { ProductSubcategories.productId eq productId }
            .map { it[ProductSubcategories.subcategoryId] }
    }

    fun insertProductSubcategories(productId: Long, subcategoryIds: List<Long>) = transaction {
        // Получаем список ID категорий, к которым привязан продукт
        val allowedCategoryIds = ProductCategories
            .selectAll().where { ProductCategories.productId eq productId }
            .map { it[ProductCategories.categoryId] }

        // Получаем подкатегории, проверяем что они принадлежат разрешённым категориям
        val subcategories = Subcategories
            .selectAll().where { Subcategories.id inList subcategoryIds }
            .associateBy { it[Subcategories.id] }

        // 3. Фильтруем только те, которые принадлежат разрешённым категориям
        val filteredSubcategories = subcategories.filter { (_, row) ->
            row[Subcategories.categoryId] in allowedCategoryIds
        }

        // 4. Проверяем, все ли подкатегории валидны
        if (filteredSubcategories.size != subcategoryIds.size) {
            val invalidIds = subcategoryIds.toSet() - filteredSubcategories.keys
            error(
                HttpStatusCode.BadRequest,
                mapOf("error" to "invalid_subcategories", "message" to "Некорректные подкатегории: $invalidIds")
            )
        }

        // Вставляем допустимые подкатегории
        ProductSubcategories.batchInsert(filteredSubcategories.keys.toList()) { subcategoryId ->
            this[ProductSubcategories.productId] = productId
            this[ProductSubcategories.subcategoryId] = subcategoryId
        }
    }

    fun getProductSubcategories(productId: Long): List<SubcategoryResponse> = transaction {
        (ProductSubcategories innerJoin Subcategories)
            .leftJoin(SubcategoryTranslations)
            .selectAll()
            .where { ProductSubcategories.productId eq productId }
            .groupBy { it[Subcategories.id] }
            .mapNotNull { (subcategoryId, rows) ->
                val firstRow = rows.firstOrNull() ?: return@mapNotNull null

                val fallbackName = firstRow[Subcategories.name]
                val fallbackCategoryId = firstRow[Subcategories.categoryId]
                val fallbackImage = firstRow.getOrNull(Subcategories.imageUrl)

                val translations = rows.mapNotNull { row ->
                    row.getOrNull(SubcategoryTranslations.id)?.let {
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
                    name = fallbackName,
                    categoryId = fallbackCategoryId,
                    translations = translations,
                    imageUrl = fallbackImage?.let {
                        "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(it)
                    }
                )
            }
    }
}