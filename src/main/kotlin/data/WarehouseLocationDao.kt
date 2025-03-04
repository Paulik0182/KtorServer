package com.example.data

import com.example.WarehouseLocations
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.insertReturning
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object WarehouseLocationDao {

    fun getAll(): List<ResultRow> = transaction {
        WarehouseLocations.selectAll().toList()
    }

    fun getById(id: Int): ResultRow? = transaction {
        WarehouseLocations
            .selectAll().where { WarehouseLocations.id eq id }
            .singleOrNull()
    }

    fun insert(counterpartyId: Int, locationCode: String): Int = transaction {
        WarehouseLocations.insertReturning(listOf(WarehouseLocations.id)) {
            it[WarehouseLocations.counterpartyId] = counterpartyId
            it[WarehouseLocations.locationCode] = locationCode
        }.single()[WarehouseLocations.id]
    }

    fun delete(id: Int) = transaction {
        WarehouseLocations.deleteWhere { WarehouseLocations.id eq id }
    }
}
