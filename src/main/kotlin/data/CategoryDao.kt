package com.example.data

import com.example.Categories
import com.example.CategoryTranslations
import com.example.Subcategories
import com.example.SubcategoryTranslations
import com.example.data.dto.category.CategoryRequest
import com.example.data.dto.dictionaries.CategoryResponse
import com.example.data.dto.dictionaries.CategoryTranslationResponse
import com.example.data.dto.dictionaries.SubcategoryResponse
import com.example.data.dto.dictionaries.SubcategoryTranslationResponse
import java.util.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList

object CategoryDao {

    suspend fun getAllCategoriesWithSubcategories(): List<CategoryResponse> = dbQuery {
        val categories = Categories.selectAll().map { row ->
            val categoryId = row[Categories.id]
            val subcategories = Subcategories.selectAll().where { Subcategories.categoryId eq categoryId }.map { subRow ->
                val subcategoryId = subRow[Subcategories.id]
                SubcategoryResponse(
                    id = subcategoryId,
                    name = subRow[Subcategories.name],
                    imageUrl = subRow[Subcategories.imageUrl]?.let { Base64.getEncoder().encodeToString(it) },
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
                imageUrl = row[Categories.imageUrl]?.let { Base64.getEncoder().encodeToString(it) },
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
                    imageUrl = subRow[Subcategories.imageUrl]?.let { Base64.getEncoder().encodeToString(it) },
                    categoryId = id,
                    translations = trans
                )
            }

        CategoryResponse(
            id = id,
            name = row[Categories.name],
            imageUrl = row[Categories.imageUrl]?.let { Base64.getEncoder().encodeToString(it) },
            translations = translations,
            subcategories = subcategories
        )
    }

    suspend fun insertCategory(request: CategoryRequest): Long = dbQuery {
        val categoryId = Categories.insert {
            it[name] = request.name
            it[imageUrl] = decodeBase64OrNull(request.imageBase64)
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
                it[imageUrl] = decodeBase64OrNull(sub.imageBase64)
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
            it[imageUrl] = decodeBase64OrNull(request.imageBase64)
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
                it[imageUrl] = decodeBase64OrNull(sub.imageBase64)
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
                        imageUrl = sub[Subcategories.imageUrl]?.let { Base64.getEncoder().encodeToString(it) },
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
                    imageUrl = row[Categories.imageUrl]?.let { Base64.getEncoder().encodeToString(it) },
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
            it[imageUrl] = decodeBase64OrNull(patch.imageBase64)
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
                it[imageUrl] = decodeBase64OrNull(sub.imageBase64)
            } get Subcategories.id)

            if (sub.id != null) {
                Subcategories.update({ Subcategories.id eq subId }) {
                    it[name] = sub.name
                    it[imageUrl] = decodeBase64OrNull(sub.imageBase64)
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
                imageUrl = row[Subcategories.imageUrl]?.let { Base64.getEncoder().encodeToString(it) },
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

    fun decodeBase64OrNull(data: String?): ByteArray? = try {
        data?.let { Base64.getDecoder().decode(it) }
    } catch (e: IllegalArgumentException) {
        null
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
