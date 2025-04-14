package com.example.data

import com.example.*
import com.example.data.dto.category.CategoryRequest
import com.example.data.dto.dictionaries.CategoryResponse
import com.example.data.dto.dictionaries.CategoryTranslationResponse
import com.example.data.dto.dictionaries.SubcategoryResponse
import com.example.data.dto.dictionaries.SubcategoryTranslationResponse
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object CategoryDao {

    suspend fun getAllCategoriesWithSubcategories(): List<CategoryResponse> = dbQuery {
        val categories = Categories.selectAll().map { row ->
            val categoryId = row[Categories.id]
            val subcategories =
                Subcategories.selectAll().where { Subcategories.categoryId eq categoryId }.map { subRow ->
                    val subcategoryId = subRow[Subcategories.id]
                    SubcategoryResponse(
                        id = subcategoryId,
                        name = subRow[Subcategories.name],
                        imageUrl = subRow[Subcategories.imagePath]?.let { path ->
                            "/uploads/images/${File(path).name}"
                        } ?: "/uploads/images/placeholder.png",
                        categoryId = subRow[Subcategories.categoryId],
                        translations = SubcategoryTranslations
                            .selectAll().where { SubcategoryTranslations.subcategoryId eq subcategoryId }
                            .map {
                                SubcategoryTranslationResponse(
                                    id = it[SubcategoryTranslations.id],
                                    subcategoryId = it[SubcategoryTranslations.subcategoryId],
                                    languageCode = it[SubcategoryTranslations.languageCode],
                                    name = it[SubcategoryTranslations.name]
                                )
                            }
                    )
                }

            CategoryResponse(
                id = categoryId,
                name = row[Categories.name],
                imageUrl = row[Categories.imagePath]?.let { path ->
                    "/uploads/images/${File(path).name}"
                } ?: "/uploads/images/placeholder.png",
                translations = CategoryTranslations
                    .selectAll().where { CategoryTranslations.categoryId eq categoryId }
                    .map {
                        CategoryTranslationResponse(
                            id = it[CategoryTranslations.id],
                            categoryId = it[CategoryTranslations.categoryId],
                            languageCode = it[CategoryTranslations.languageCode],
                            name = it[CategoryTranslations.name]
                        )
                    },
                subcategories = subcategories
            )
        }

        categories
    }

    suspend fun getById(id: Long): CategoryResponse? = dbQuery {
        val row = Categories.selectAll().where { Categories.id eq id }.singleOrNull() ?: return@dbQuery null

        val translations = CategoryTranslations.selectAll().where { CategoryTranslations.categoryId eq id }
            .map {
                CategoryTranslationResponse(
                    id = it[CategoryTranslations.id],
                    categoryId = id,
                    languageCode = it[CategoryTranslations.languageCode],
                    name = it[CategoryTranslations.name]
                )
            }

        val subcategories = Subcategories.selectAll().where { Subcategories.categoryId eq id }
            .map { subRow ->
                val subId = subRow[Subcategories.id]
                val trans = SubcategoryTranslations
                    .selectAll().where { SubcategoryTranslations.subcategoryId eq subId }
                    .map {
                        SubcategoryTranslationResponse(
                            id = it[SubcategoryTranslations.id],
                            subcategoryId = subId,
                            languageCode = it[SubcategoryTranslations.languageCode],
                            name = it[SubcategoryTranslations.name]
                        )
                    }

                SubcategoryResponse(
                    id = subId,
                    name = subRow[Subcategories.name],
                    imageUrl = subRow[Subcategories.imagePath]?.let { path ->
                        "/uploads/images/${File(path).name}"
                    } ?: "/uploads/images/placeholder.png",
                    categoryId = id,
                    translations = trans
                )
            }

        CategoryResponse(
            id = id,
            name = row[Categories.name],
            imageUrl = row[Categories.imagePath]?.let { path ->
                "/uploads/images/${File(path).name}"
            } ?: "/uploads/images/placeholder.png",
            translations = translations,
            subcategories = subcategories
        )
    }

    suspend fun insertCategory(request: CategoryRequest): Long = dbQuery {
        val categoryId = Categories.insert {
            it[name] = request.name
            it[imagePath] = request.imagePath
        } get Categories.id

        request.translations.forEach { t ->
            CategoryTranslations.insert {
                it[CategoryTranslations.categoryId] = categoryId
                it[languageCode] = t.languageCode
                it[name] = t.name
            }.getOrNull(CategoryTranslations.id)
        }

        request.subcategories.forEach { sub ->
            val subId = Subcategories.insert {
                it[Subcategories.categoryId] = categoryId
                it[name] = sub.name
                it[imagePath] = sub.imagePath
            } get Subcategories.id

            sub.translations.forEach { tr ->
                SubcategoryTranslations.insert {
                    it[subcategoryId] = subId
                    it[languageCode] = tr.languageCode
                    it[name] = tr.name
                }
            }
        }

        categoryId
    }

    suspend fun updateCategory(categoryId: Long, request: CategoryRequest): Boolean = dbQuery {
        val updated = Categories.update({ Categories.id eq categoryId }) {
            it[name] = request.name
            it[imagePath] = request.imagePath
        }

        if (updated == 0) return@dbQuery false

        // Переводы категории: удаляем и добавляем заново
        CategoryTranslations.deleteWhere { CategoryTranslations.categoryId eq categoryId }
        request.translations.forEach { tr ->
            CategoryTranslations.insert {
                it[CategoryTranslations.categoryId] = categoryId
                it[languageCode] = tr.languageCode
                it[name] = tr.name
            } get CategoryTranslations.id
        }

        // Удалим все подкатегории и переводы
        val oldSubIds = Subcategories
            .selectAll().where { Subcategories.categoryId eq categoryId }
            .map { it[Subcategories.id] }

        SubcategoryTranslations.deleteWhere { SubcategoryTranslations.subcategoryId inList oldSubIds }
        Subcategories.deleteWhere { Subcategories.categoryId eq categoryId }

        request.subcategories.forEach { sub ->
            val subId = Subcategories.insert {
                it[Subcategories.categoryId] = categoryId
                it[name] = sub.name
                it[imagePath] = sub.imagePath
            } get Subcategories.id

            sub.translations.forEach { tr ->
                SubcategoryTranslations.insert {
                    it[subcategoryId] = subId
                    it[languageCode] = tr.languageCode
                    it[name] = tr.name
                }
            }
        }
        true
    }

    suspend fun getCategoryById(categoryId: Long): CategoryResponse? = dbQuery {
        Categories.selectAll().where { Categories.id eq categoryId }
            .mapNotNull { row ->
                val subs = Subcategories.selectAll().where { Subcategories.categoryId eq categoryId }.map { sub ->
                    val subId = sub[Subcategories.id]
                    SubcategoryResponse(
                        id = subId,
                        name = sub[Subcategories.name],
                        imageUrl = sub[Subcategories.imagePath]?.let { path ->
                            "/uploads/images/${File(path).name}"
                        } ?: "/uploads/images/placeholder.png",
                        categoryId = sub[Subcategories.categoryId],
                        translations = SubcategoryTranslations
                            .selectAll().where { SubcategoryTranslations.subcategoryId eq subId }
                            .map {
                                SubcategoryTranslationResponse(
                                    id = it[SubcategoryTranslations.id],
                                    subcategoryId = it[SubcategoryTranslations.subcategoryId],
                                    languageCode = it[SubcategoryTranslations.languageCode],
                                    name = it[SubcategoryTranslations.name]
                                )
                            }
                    )
                }

                CategoryResponse(
                    id = row[Categories.id],
                    name = row[Categories.name],
                    imageUrl = row[Categories.imagePath]?.let { path ->
                        "/uploads/images/${File(path).name}"
                    } ?: "/uploads/images/placeholder.png",
                    translations = CategoryTranslations
                        .selectAll().where { CategoryTranslations.categoryId eq categoryId }
                        .map {
                            CategoryTranslationResponse(
                                id = it[CategoryTranslations.id],
                                categoryId = it[CategoryTranslations.categoryId],
                                languageCode = it[CategoryTranslations.languageCode],
                                name = it[CategoryTranslations.name]
                            )
                        },
                    subcategories = subs
                )
            }
            .singleOrNull()
    }

    suspend fun deleteCategory(id: Long): Boolean = dbQuery {
        val deleted = Categories.deleteWhere { Categories.id eq id }
        deleted > 0
    }

    suspend fun patchCategory(categoryId: Long, patch: CategoryRequest): Boolean = dbQuery {
        val updated = Categories.update({ Categories.id eq categoryId }) {
            it[name] = patch.name
            it[imagePath] = patch.imagePath
        }

        if (updated == 0) return@dbQuery false

        // Обновляем только переводы, если есть
        patch.translations.forEach { tr ->
            CategoryTranslations.upsert {
                it[CategoryTranslations.categoryId] = categoryId
                it[languageCode] = tr.languageCode
                it[name] = tr.name
            }
        }

        // Обновляем или добавляем подкатегории
        patch.subcategories.forEach { sub ->
            val subId = sub.id ?: (Subcategories.insert {
                it[Subcategories.categoryId] = categoryId
                it[name] = sub.name
                it[imagePath] = sub.imagePath
            } get Subcategories.id)

            if (sub.id != null) {
                Subcategories.update({ Subcategories.id eq subId }) {
                    it[name] = sub.name
                    it[imagePath] = sub.imagePath
                }
            }

            // Обновляем переводы
            sub.translations.forEach { tr ->
                SubcategoryTranslations.upsert {
                    it[subcategoryId] = subId
                    it[languageCode] = tr.languageCode
                    it[name] = tr.name
                }
            }
        }
        true
    }

    suspend fun getAllSubcategories(): List<SubcategoryResponse> = dbQuery {
        Subcategories.selectAll().map { row ->
            val id = row[Subcategories.id]
            SubcategoryResponse(
                id = id,
                name = row[Subcategories.name],
                imageUrl = row[Subcategories.imagePath]?.let { path ->
                    "/uploads/images/${File(path).name}"
                } ?: "/uploads/images/placeholder.png",
                categoryId = row[Subcategories.categoryId],
                translations = SubcategoryTranslations
                    .selectAll().where { SubcategoryTranslations.subcategoryId eq id }
                    .map {
                        SubcategoryTranslationResponse(
                            id = it[SubcategoryTranslations.id],
                            subcategoryId = it[SubcategoryTranslations.subcategoryId],
                            languageCode = it[SubcategoryTranslations.languageCode],
                            name = it[SubcategoryTranslations.name]
                        )
                    }
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
                Subcategories.imagePath,
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
                val fallbackImage = firstRow.getOrNull(Subcategories.imagePath)

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
                    imageUrl = fallbackImage?.let { path ->
                        "/uploads/images/${File(path).name}"
                    } ?: "/uploads/images/placeholder.png",
                )
            }

        return@transaction subcategories
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
                val fallbackImage = firstRow.getOrNull(Subcategories.imagePath)

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
                    imageUrl = fallbackImage?.let { path ->
                        "/uploads/images/${File(path).name}"
                    } ?: "/uploads/images/placeholder.png",
                )
            }
    }

    // Получение категорий товара
    fun getProductCategories(productId: Long): List<CategoryResponse> = transaction {
        // Все подкатегории, к которым привязаны товары
        val subcategoriesWithProducts = ProductSubcategories
            .selectAll()
            .map { it[ProductSubcategories.subcategoryId] }
            .toSet()

        // Категории, связанные с этим продуктом
        val categoryRows = ProductCategories
            .innerJoin(Categories)
            .leftJoin(CategoryTranslations)
            .selectAll()
            .where { ProductCategories.productId eq productId }

        val groupedByCategory = categoryRows.groupBy { it[Categories.id] }

        return@transaction groupedByCategory.mapNotNull { (categoryId, rows) ->
            val firstRow = rows.firstOrNull() ?: return@mapNotNull null

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

            // ВАЖНО: Показываем только подкатегории, у которых есть хотя бы один продукт
            // Если нужно чтобы в категориях отображались все подкатегории, в том числе без продуктов, Убрать фильтр.
            val nonEmptySubcategories = allSubcategories.filter { it.id in subcategoriesWithProducts }

            // Собираем результат
            CategoryResponse(
                id = categoryId,
                name = firstRow[Categories.name],
                translations = translations,
                subcategories = nonEmptySubcategories,
                imageUrl = firstRow.getOrNull(Categories.imagePath)
                    ?.let { path ->
                        "/uploads/images/${File(path).name}"
                    } ?: "/uploads/images/placeholder.png",
            )
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
