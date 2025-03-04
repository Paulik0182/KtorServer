package com.example

import com.example.data.dto.ProductImageResponse
import com.example.data.dto.ProductLinkResponse
import com.example.data.dto.WarehouseLocationResponse
import org.jetbrains.exposed.sql.ReferenceOption
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
    val stockQuantity = integer("stock_quantity").default(0)
    val minStockQuantity = integer("min_stock_quantity").default(0)
    val isDemanded = bool("is_demanded").default(true)

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

object WarehouseLocations : Table("warehouse_locations") {
    val id = integer("id").autoIncrement()
    val counterpartyId = integer("counterparty_id").references(Counterparties.id, onDelete = ReferenceOption.CASCADE)
    val locationCode = varchar("location_code", 50)

    override val primaryKey = PrimaryKey(id)
}

object ProductLocations : Table("product_locations") {
    val productId = integer("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val locationId = integer("location_id").references(WarehouseLocations.id, onDelete = ReferenceOption.CASCADE)
    override val primaryKey = PrimaryKey(productId, locationId)
}

object ProductImages : Table("product_images") {
    val id = integer("id").autoIncrement()
    val productId = integer("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val image = binary("image")
    override val primaryKey = PrimaryKey(id)
}

object ProductLinks : Table("product_links") {
    val id = integer("id").autoIncrement()
    val productId = integer("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val counterpartyId = integer("counterparty_id").references(Counterparties.id, onDelete = ReferenceOption.CASCADE)
    val url = text("url")

    override val primaryKey = PrimaryKey(id)
}

object ProductCodes : Table("product_codes") {
    val productId = integer("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val code = varchar("code", 50) // Код продукта, максимальная длина 50 символов

    override val primaryKey = PrimaryKey(productId, code) // Комбинированный первичный ключ
}
