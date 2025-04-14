package com.example.data

import com.example.ProductImages
import com.example.data.dto.product.ProductImageResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

object ProductImageDao {

    fun getProductImages(productId: Long): List<ProductImageResponse> = transaction {
        ProductImages.selectAll().where { ProductImages.productId eq productId }
            .map {
                ProductImageResponse(
                    id = it[ProductImages.id],
                    productId = it[ProductImages.productId],
                    imageUrl = it[ProductImages.imagePath]?.let { path ->
                        "/uploads/images/${File(path).name}"
                    } ?: "/uploads/images/placeholder.png",
                    position = it[ProductImages.position]
                )
            }
    }

    fun insertProductImagePath(productId: Long, imagePath: String): Long = transaction {
        val maxPosition = ProductImages
            .selectAll().where { ProductImages.productId eq productId }
            .maxOfOrNull { it[ProductImages.position] } ?: -1


        val insertedId = ProductImages.insertReturning {
            it[ProductImages.productId] = productId
            it[ProductImages.imagePath] = imagePath
            it[position] = maxPosition + 1
        }.single()[ProductImages.id]
        return@transaction insertedId // или null, если не нужен ID
    }

    fun getProductImageById(imageId: Long): ProductImageResponse? = transaction {
        ProductImages.selectAll().where { ProductImages.id eq imageId }
            .map {
                ProductImageResponse(
                    id = it[ProductImages.id],
                    productId = it[ProductImages.productId],
                    imageUrl = it[ProductImages.imagePath]?.let { path ->
                        "/uploads/images/${File(path).name}"
                    } ?: "/uploads/images/placeholder.png",
                    position = it[ProductImages.position]
                )
            }
            .firstOrNull()
    }

    fun deleteProductImage(imageId: Long): Boolean = transaction {
        val image = ProductImages.selectAll().where { ProductImages.id eq imageId }.firstOrNull() ?: return@transaction false
        val path = image[ProductImages.imagePath]

        // Удаляем из файловой системы
        val deletedFromDisk = File(path).delete()

        // Удаляем из БД
        val deletedFromDb = ProductImages.deleteWhere { ProductImages.id eq imageId } > 0

        deletedFromDisk && deletedFromDb
    }


    fun reorderProductImages(productId: Long, imageIds: List<Long>) = transaction {
        imageIds.forEachIndexed { index, imageId ->
            ProductImages.update({
                ProductImages.id eq imageId and (ProductImages.productId eq productId)
            }) {
                it[position] = index
            }
        }
    }
}
