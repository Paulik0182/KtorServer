package com.example.data

import com.example.OrderItems
import com.example.Orders
import com.example.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Объект OrderDao управляет операциями с заказами в базе данных. Этот объект отвечает за выполнение CRUD-операций
 * (Create, Read, Update, Delete) с таблицей orders.
 */
object OrderDao {

    /**
     * Получение всех заказов из таблицы orders.
     *
     * transaction {} — оборачивает операцию в транзакцию, чтобы все изменения выполнялись безопасно и атомарно.
     * Orders.selectAll() — выбирает все строки из таблицы orders.
     * .toList() — преобразует результат в список (List<ResultRow>).
     * Возвращает список всех заказов.
     */
    fun getAll(): List<ResultRow> = transaction {
        Orders.selectAll().toList()
    }

    /**
     * Находим заказ по ID.
     *
     * Orders.select { Orders.id eq id } — выбирает строки, где id соответствует переданному значению.
     * .singleOrNull() — если найдена только одна строка, возвращает её, иначе null.
     * Возвращает один заказ, если найден, или null, если такого ID нет.
     */
//    fun getById(id: Int): ResultRow? = transaction {
//        Orders
//            .selectAll().where { Orders.id eq id }
//            .singleOrNull()
//    }

    /**
     * создает новый заказ и возвращает его ID.
     *
     * Orders.insert {} — вставляет новую запись в таблицу orders.
     * it[Orders.counterpartyId] = counterpartyId — устанавливает counterparty_id (ID клиента/поставщика).
     * get Orders.id — получает автоматически сгенерированный ID нового заказа.
     * Создает новый заказ и возвращает его id.
     */
//    fun insert(counterpartyId: Int): Int = transaction {
//        Orders.insert {
//            it[Orders.counterpartyId] = counterpartyId
//        } get Orders.id
//    }

    /**
     * удаляет заказ по ID.
     *
     * Orders.deleteWhere { Orders.id eq id } — удаляет заказ, если id совпадает.
     * Удаляет заказ только из orders, но не удаляет связанные товары в order_items.
     */
//    fun delete(id: Int) = transaction {
//        Orders.deleteWhere { Orders.id eq id }
//    }

    /**
     * удаляет заказ и все связанные с ним товары в order_items.
     *
     * OrderItems.deleteWhere { OrderItems.orderId eq id } — сначала удаляем все товары, связанные с заказом.
     * Orders.deleteWhere { Orders.id eq id } — затем удаляем сам заказ.
     * Гарантирует, что перед удалением заказа очищаются все связанные записи в order_items.
     */
//    fun deleteWithItems(id: Int) = transaction {
//        // Удаляем все элементы заказа перед удалением самого заказа
//        OrderItems.deleteWhere { OrderItems.orderId eq id }
//        Orders.deleteWhere { Orders.id eq id }
//    }

    /**
     * получает список товаров для конкретного заказа.
     *
     * OrderItems.innerJoin(Products) — объединяет order_items и products по product_id.
     * .select { OrderItems.orderId eq orderId } — выбирает только те записи, у которых order_id соответствует переданному значению.
     * .toList() — преобразует в список.
     * Возвращает список товаров, включенных в указанный заказ.
     */
//    fun getOrderItems(orderId: Int): List<ResultRow> = transaction {
//        OrderItems
//            .innerJoin(Products)
//            .selectAll().where { OrderItems.orderId eq orderId }
//            .toList()
//    }

    /**
     * Обновление контрагента заказа
     *
     * Orders.update({ Orders.id eq id }) — выполняем UPDATE в таблице Orders, находя запись по id.
     * { Orders.id eq id } — фильтр, указывающий какую строку обновлять (WHERE id = ?).
     */
//    fun update(id: Int, counterpartyId: Int) = transaction {
//        Orders.update({ Orders.id eq id }) {
//            it[Orders.counterpartyId] = counterpartyId
//        }
//    }
}
