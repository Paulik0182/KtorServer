package com.example.data

import com.example.ProductLinks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object ProductLinkDao {

    fun getByProduct(productId: Int): List<ResultRow> = transaction {
        ProductLinks
            .selectAll().where { ProductLinks.productId eq productId }
            .toList()
    }

    fun insert(productId: Int, counterpartyId: Int, url: String): Int = transaction {
        ProductLinks
            .insertReturning(listOf(ProductLinks.id)) {
                it[ProductLinks.productId] = productId
                it[ProductLinks.counterpartyId] = counterpartyId
                it[ProductLinks.url] = url
            }
            .single()[ProductLinks.id]
    }

    fun delete(id: Int) = transaction {
        ProductLinks.deleteWhere { ProductLinks.id eq id }
    }
}
