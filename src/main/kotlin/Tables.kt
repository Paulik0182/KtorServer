package com.example

import com.example.Categories.nullable
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Counterparties : Table("counterparties") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100)
    val type = varchar("type", 50)
    val isSupplierOld = bool("is_supplier_old").default(false)
    val productCountOld = integer("product_count_old").default(0)
    val isSupplier = bool("is_supplier_type").default(false)
    val isWarehouse = bool("is_warehouse_type").default(false)
    val isCustomer = bool("is_customer_type").default(false)

    override val primaryKey = PrimaryKey(id)
}

object ProductCounterparties : Table("product_counterparties") {
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val counterpartyId = long("counterparty_id").references(Counterparties.id, onDelete = ReferenceOption.CASCADE)
    val stockQuantity = integer("stock_quantity").default(0)
    val role = varchar("role", 50)
    val minStockQuantity = integer("min_stock_quantity").default(0)
    val warehouseLocationCodes = stringListJsonb("warehouse_location_codes").nullable()
    val measurementUnitId = long("measurement_unit_id").references(MeasurementUnits.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(productId, counterpartyId)
}

object Products : Table("products") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val hasSuppliers = bool("has_suppliers").default(false)
    val supplierCount = integer("supplier_count").default(0)
    val totalStockQuantity = integer("total_stock_quantity").default(0)
    val minStockQuantity = integer("min_stock_quantity").default(0)
    val isDemanded = bool("is_demanded").default(true)
    val measurementUnitId = long("measurement_unit_id").references(MeasurementUnits.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}

object Orders : Table("orders") {
    val id = long("id").autoIncrement()
    val counterpartyId = long("counterparty_id").references(Counterparties.id, onDelete = ReferenceOption.CASCADE)
    val orderStatus = integer("order_status").default(0)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").nullable()
    val acceptedAt = timestamp("accepted_at").nullable()
    val completedAt = timestamp("completed_at").nullable()

    override val primaryKey = PrimaryKey(id)
}

object OrderItems : Table("order_items") {
    val id = long("id").autoIncrement()
    val orderId = long("order_id").references(Orders.id, onDelete = ReferenceOption.CASCADE)
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val measurementUnitId = long("measurement_unit_id").references(MeasurementUnits.id, onDelete = ReferenceOption.CASCADE)
    val quantity = integer("quantity").default(1)

    override val primaryKey = PrimaryKey(id)
}

object ProductSuppliers : Table("product_suppliers") {
    val id = long("id").autoIncrement() // ? возможно не нужно поле!
    val productId = long("product_id").references(Products.id)
    val supplierId = long("supplier_id").references(Counterparties.id)

    override val primaryKey = PrimaryKey(productId, supplierId)
}

object ProductImages : Table("product_images") {
    val id = long("id").autoIncrement()
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val imageBase64 = binary("image")

    override val primaryKey = PrimaryKey(id)
}

object ProductLinks : Table("product_links") {
    val id = long("id").autoIncrement()
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val counterpartyId = long("counterparty_id").references(Counterparties.id, onDelete = ReferenceOption.CASCADE)
    val urlId = long("url_id").references(Urls.id)

    override val primaryKey = PrimaryKey(id)
}

object ProductCodes : Table("product_codes") {
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val codeId = long("code_id").references(Codes.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(productId, codeId) // Комбинированный первичный ключ
}

object Categories : Table("categories") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100)
    val imageUrl = binary("image").nullable()

    override val primaryKey = PrimaryKey(id)
}

object CategoryTranslations : Table("category_translations") {
    val id = long("id").autoIncrement()
    val categoryId = long("category_id").references(Categories.id, onDelete = ReferenceOption.CASCADE)
    val languageCode = varchar("language_code", 5)
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object Countries : Table("countries") {
    val id = long("id").autoIncrement()
    val name = varchar("country_name", 100)
    val phoneCode = varchar("phone_code", 10)
    val isoCode = varchar("iso_code", 10)

    override val primaryKey = PrimaryKey(id)
}

object CountryTranslations : Table("countries_translations") {
    val id = long("id").autoIncrement()
    val countryId = long("country_id").references(Countries.id, onDelete = ReferenceOption.CASCADE)
    val languageCode = varchar("language_code", 10)
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object Cities : Table("cities") {
    val id = long("id").autoIncrement()
    val countryId = long("country_id").references(Countries.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("city_name", 100)

    override val primaryKey = PrimaryKey(id)
}

object CityTranslations : Table("cities_translations") {
    val id = long("id").autoIncrement()
    val cityId = long("city_id").references(Cities.id, onDelete = ReferenceOption.CASCADE)
    val languageCode = varchar("language_code", 5)
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object Subcategories : Table("subcategories") {
    val id = long("id").autoIncrement()
    val categoryId = long("category_id").references(Categories.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 100)
    val imageUrl = binary("image").nullable()

    override val primaryKey = PrimaryKey(id)
}

object SubcategoryTranslations : Table("subcategory_translations") {
    val id = long("id").autoIncrement()
    val subcategoryId = long("subcategory_id").references(Subcategories.id, onDelete = ReferenceOption.CASCADE)
    val languageCode = varchar("language_code", 5)
    val name = varchar("name", 100)

    override val primaryKey = PrimaryKey(id)
}

object ProductCategories : Table("product_categories") {
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val categoryId = long("category_id").references(Categories.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(productId, categoryId)
}

object MeasurementUnits : Table("measurement_units") {
    val id = long("id").autoIncrement()
    val name = varchar("name", 100)
    val abbreviation = varchar("abbreviation", 10).nullable()

    override val primaryKey = PrimaryKey(id)
}

object MeasurementUnitTranslations : Table("measurement_units_translations") {
    val id = long("id").autoIncrement()
    val measurementUnitId = long("measurement_unit_id").references(MeasurementUnits.id, onDelete = ReferenceOption.CASCADE)
    val languageCode = varchar("language_code", 5)
    val name = varchar("name", 100)
    val abbreviation = varchar("abbreviation", 10).nullable()

    override val primaryKey = PrimaryKey(id)
}

object CounterpartyAddresses : Table("counterparty_addresses") {
    val id = long("id").autoIncrement()
    val counterpartyId = long("counterparty_id").references(Counterparties.id, onDelete = ReferenceOption.CASCADE)
    val countryId = long("country_id").references(Countries.id, onDelete = ReferenceOption.CASCADE)
    val postalCode = varchar("postal_code", 20).nullable()
    val streetName = varchar("street_name", 255).nullable()
    val houseNumber = varchar("house_number", 50).nullable()
    val locationNumber = varchar("location_number", 50).nullable()
    val latitude = decimal("latitude", 10, 6).nullable()
    val longitude = decimal("longitude", 10, 6).nullable()

    override val primaryKey = PrimaryKey(id)
}

object CounterpartyContacts : Table("counterparty_contacts") {
    val id = long("id").autoIncrement()
    val counterpartyId = long("counterparty_id").references(Counterparties.id, onDelete = ReferenceOption.CASCADE)
    val contactType = varchar("contact_type", 50)
    val contactValue = varchar("contact_value", 100)
    val countryCodeId = long("country_code_id").references(Countries.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(id)
}

object Urls : Table("urls") {
    val id = long("id").autoIncrement()
    val url = text("url")

    override val primaryKey = PrimaryKey(id)
}

object Codes : Table("codes") {
    val id = long("id").autoIncrement()
    val code = varchar("code", 50) // Код продукта, максимальная длина 50 символов

    override val primaryKey = PrimaryKey(id)
}

object ProductSubcategories : Table("product_subcategories") {
    val productId = long("product_id").references(Products.id, onDelete = ReferenceOption.CASCADE)
    val subcategoryId = long("subcategory_id").references(Subcategories.id, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(productId, subcategoryId)
}

