package com.example.data

import com.example.Counterparties
import com.example.ProductSuppliers
import com.example.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Этот объект CounterpartyDao — это DAO (Data Access Object) для работы с таблицей Counterparties в базе данных.
 * Он оборачивает SQL-запросы в удобные функции.
 */
object CounterpartyDao {

    /**
     * transaction {} — оборачивает код в транзакцию (гарантирует, что код будет выполняться атомарно).
     * Counterparties.selectAll() — SQL-аналог SELECT * FROM counterparties;, выбирает все строки.
     * .toList() — преобразует результат в список List<ResultRow> (каждая строка в таблице превращается в объект ResultRow).
     * Возвращает:
     * Список всех контрагентов (List<ResultRow>), каждый из которых содержит id, name, type, isSupplier, productCount.
     */
    fun getAll(): List<ResultRow> = transaction {
        Counterparties.selectAll().toList()
    }

    /**
     * Получение контрагента по ID
     *
     * Counterparties.select { Counterparties.id eq id } —
     * SELECT * FROM counterparties WHERE id = ?;
     * Фильтруем контрагента по id.
     * .singleOrNull() —
     * Если строка найдена → возвращает ResultRow (контрагент).
     * Если строки нет → возвращает null.
     */
//    fun getById(id: Int): ResultRow? = transaction {
//        Counterparties
//            .selectAll().where { Counterparties.id eq id }
//            .singleOrNull()
//    }

    /**
     * Добавление нового контрагента
     *
     * Counterparties.insert {} — вставка новой строки (INSERT INTO counterparties (name, type) VALUES (?, ?);).
     * it[Counterparties.name] = name — записываем имя.
     * it[Counterparties.type] = type — записываем тип (например, "поставщик" или "клиент").
     * .get(Counterparties.id) — получаем ID вновь созданного контрагента.
     */
//    fun insert(name: String, type: String): Int = transaction {
//        Counterparties.insert {
//            it[Counterparties.name] = name
//            it[Counterparties.type] = type
//        } get Counterparties.id
//    }

    /**
     * Удаление контрагента
     *
     * deleteWhere { Counterparties.id eq id } —
     * DELETE FROM counterparties WHERE id = ?;
     * Удаляет контрагента без проверки зависимостей.
     * Возвращает:
     * Ничего (Unit). Просто удаляет запись.
     */
//    fun delete(id: Int) = transaction {
//        Counterparties.deleteWhere { Counterparties.id eq id }
//    }

    /**
     * Удаление контрагента с очисткой связей
     *
     * ProductSuppliers.deleteWhere { ProductSuppliers.supplierId eq id }
     * DELETE FROM product_suppliers WHERE supplier_id = ?;
     * Удаляет связь между поставщиком и товарами.
     * Counterparties.deleteWhere { Counterparties.id eq id }
     * DELETE FROM counterparties WHERE id = ?;
     * Далее удаляем контрагента.
     * Возвращает:
     * Если поставщик связан с товарами в product_suppliers, то обычный delete() вызовет ошибку из-за внешних ключей.
     * deleteWithProducts() сначала очищает связи, а потом удаляет контрагента.
     */
//    fun deleteWithProducts(id: Int) = transaction {
//        // Удаляем связь поставщика с товарами перед удалением
//        ProductSuppliers.deleteWhere { ProductSuppliers.supplierId eq id }
//        Counterparties.deleteWhere { Counterparties.id eq id }
//    }

    /**
     * Получение товаров, поставляемых поставщиком
     *
     * .innerJoin(Products) —
     * SQL-аналог SELECT * FROM product_suppliers INNER JOIN products ON product_suppliers.product_id = products.id;
     * Связывает таблицы product_suppliers и products.
     * .select { ProductSuppliers.supplierId eq supplierId } —
     * WHERE product_suppliers.supplier_id = ?
     * Выбираем только те товары, которые связаны с поставщиком.
     * .toList() — превращаем в List<ResultRow>.
     * Возвращает:
     * Список всех товаров, поставляемых данным поставщиком.
     */
//    fun getProductsBySupplier(supplierId: Int): List<ResultRow> = transaction {
//        ProductSuppliers
//            .innerJoin(Products)
//            .selectAll().where { ProductSuppliers.supplierId eq supplierId }
//            .toList()
//    }
//
//    fun update(id: Int, name: String, type: String) = transaction {
//        Counterparties.update({ Counterparties.id eq id }) {
//            it[Counterparties.name] = name
//            it[Counterparties.type] = type
//        }
//    }
}
