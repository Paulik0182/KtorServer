package com.example

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object Counterparties : Table("counterparties") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val type = varchar("type", 50)
    val isSupplier = bool("is_supplier")
    val productCount = integer("product_count").default(0)

    override val primaryKey = PrimaryKey(id)
}

object Products : Table("products") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val hasSuppliers = bool("has_suppliers").default(false)
    val supplierCount = integer("supplier_count").default(0)

    override val primaryKey = PrimaryKey(id)
}

object Orders : Table("orders") {
    val id = integer("id").autoIncrement()
    val orderDate = date("order_date")
    val counterpartyId = integer("counterparty_id").references(Counterparties.id)

    override val primaryKey = PrimaryKey(id)
}

object OrderItems : Table("order_items") {
    val id = integer("id").autoIncrement()
    val orderId = integer("order_id").references(Orders.id)
    val productId = integer("product_id").references(Products.id)
    val supplierId = integer("supplier_id").references(Counterparties.id)
    val quantity = integer("quantity")

    override val primaryKey = PrimaryKey(id)
}

object ProductSuppliers : Table("product_suppliers") {
    val productId = integer("product_id").references(Products.id)
    val supplierId = integer("supplier_id").references(Counterparties.id)

    override val primaryKey = PrimaryKey(productId, supplierId)
}