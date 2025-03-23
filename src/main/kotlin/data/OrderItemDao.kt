package com.example.data

import com.example.OrderItems
import com.example.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Этот объект управляет элементами заказа (OrderItems).
 * Он позволяет получать товары из заказа и добавлять их.
 */
object OrderItemDao {

    /**
     * Метод принимает orderId: Int — ID заказа, для которого ищем товары.
     * Возвращает список ResultRow (строк из БД).
     * = transaction {
     * Открываем транзакцию для работы с БД.
     * OrderItems.innerJoin(Products)
     * Объединяем таблицы order_items и products по product_id.
     * Это аналог SQL-запроса:
     * SELECT * FROM order_items
     * INNER JOIN products ON order_items.product_id = products.id
     * WHERE order_items.order_id = ?
     * .select { OrderItems.orderId eq orderId }
     *
     * Выбираем только элементы, принадлежащие этому заказу.
     * .toList()
     * Преобразуем результат в список List<ResultRow>.
     * Далее: Возвращаем список товаров, которые входят в указанный заказ.
     *
     * Как использовать?
     * val items = OrderItemDao.getItemsByOrder(1)
     * Получим все товары из заказа с id = 1.
     */
//    fun getItemsByOrder(orderId: Int): List<ResultRow> = transaction {
//        OrderItems
//            .innerJoin(Products)
//            .selectAll().where { OrderItems.orderId eq orderId }
//            .toList()
//    }

    // Удалить все товары из заказа
//    fun deleteItemsByOrder(orderId: Int) = transaction {
//        OrderItems.deleteWhere { OrderItems.orderId eq orderId }
//    }

    /**
     * Добавление товара в заказ
     *
     * Метод принимает:
     * orderId → ID заказа, в который добавляем товар.
     * productId → ID товара.
     * supplierId → ID поставщика товара.
     * quantity → Количество товара.
     * = transaction {Открываем транзакцию.
     * OrderItems.insert {
     *
     * Выполняем SQL-вставку (INSERT INTO order_items).
     * it[OrderItems.orderId] = orderId
     *
     * Вставляем ID заказа.
     * it[OrderItems.productId] = productId
     *
     * Вставляем ID товара.
     * it[OrderItems.supplierId] = supplierId
     *
     * Вставляем ID поставщика.
     * it[OrderItems.quantity] = quantity
     *
     * Вставляем количество.
     * } get OrderItems.id
     *
     * После вставки получаем ID новой строки.
     * Далее:
     * Добавляет товар в заказ.
     * Возвращает ID добавленного элемента.
     *
     * Как использовать?
     * val itemId = OrderItemDao.insert(1, 5, 3, 10)
     * Добавляем товар с ID = 5 в заказ №1.
     * Поставщик №3.
     * Количество 10 шт.
     * Метод вернет ID новой записи.
     */
//    fun insert(orderId: Int, productId: Int, supplierId: Int, quantity: Int): Int = transaction {
//        OrderItems.insert {
//            it[OrderItems.orderId] = orderId
//            it[OrderItems.productId] = productId
//            it[OrderItems.supplierId] = supplierId
//            it[OrderItems.quantity] = quantity
//        } get OrderItems.id
//    }
}
