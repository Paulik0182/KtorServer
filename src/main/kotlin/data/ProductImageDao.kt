package com.example.data

import com.example.ProductImages
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ProductImageDao {
    private const val MAX_IMAGE_SIZE = 10 * 1024 * 1024 // 10MB

    fun getByProduct(productId: Int): List<ResultRow> = transaction {
        ProductImages
            .selectAll().where { ProductImages.productId eq productId }
            .toList()
    }

    fun insert(productId: Int, image: ByteArray): Int = transaction {
        if (image.size > MAX_IMAGE_SIZE) {
            throw IllegalArgumentException("Размер изображения превышает 10MB")
        }

        ProductImages
            .insertReturning(listOf(ProductImages.id)) {
                it[ProductImages.productId] = productId
                it[ProductImages.image] = image
            }
            .single()[ProductImages.id]
    }

    fun delete(id: Int) = transaction {
        ProductImages.deleteWhere { ProductImages.id eq id }
    }
}
