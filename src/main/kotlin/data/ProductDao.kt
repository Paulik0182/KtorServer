package com.example.data

import com.example.Counterparties
import com.example.ProductSuppliers
import com.example.Products
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import java.math.BigDecimal

/**
 * Этот объект управляет товарами (products) в базе данных.
 * Он позволяет получать список товаров, добавлять новые, удалять и получать информацию по ID.
 */
object ProductDao {

    /**
     * Получение всех товаров
     *
     * Метод не принимает аргументов.
     * Возвращает список строк (ResultRow) из таблицы products.
     * = transaction { Открываем транзакцию.
     * Products.selectAll()
     *
     * Выполняем SQL-запрос SELECT * FROM products.
     * Получаем все записи из таблицы products.
     * .toList()
     *
     * Преобразуем результат в список List<ResultRow>.
     * Как использовать?
     * val products = ProductDao.getAll()
     * Получим все товары в виде списка.
     */
    fun getAll(): List<ResultRow> = transaction {
        Products.selectAll().toList()
    }

    /**
     * Получение товара по ID
     *
     * Метод принимает id: Int – ID товара.
     * Возвращает одну строку (ResultRow) или null, если товара нет.
     * = transaction { Открываем транзакцию.
     * Products.select { Products.id eq id }
     *
     * Выполняем SQL-запрос:
     * SELECT * FROM products WHERE id = ?
     * .singleOrNull()
     *
     * Если товар найден – вернем одну запись.
     * Если товара нет – вернем null.
     * Метод:
     * Ищет товар по ID.
     * Если товара нет – возвращает null.
     *
     * Как использовать?
     * val product = ProductDao.getById(5)
     * Найдем товар с ID = 5.
     */
    fun getById(id: Int): ResultRow? = transaction {
        Products.select { Products.id eq id }.singleOrNull()
    }

    /**
     * Добавление нового товара
     *
     * Метод принимает:
     * name: String → название товара.
     * description: String → описание товара.
     * price: Double → цена товара.
     * = transaction { Открываем транзакцию.
     * Products.insert {
     *
     * Выполняем SQL-запрос INSERT INTO products.
     * it[Products.name] = name
     *
     * Вставляем название товара.
     * it[Products.description] = description
     *
     * Вставляем описание.
     * it[Products.price] = price.toBigDecimal()
     *
     * Конвертируем Double в BigDecimal, чтобы сохранить в БД.
     * } get Products.id
     *
     * После вставки возвращаем ID новой записи.
     * Метод:
     * Добавляет новый товар в базу.
     * Возвращает его ID.
     *
     * Как использовать?
     * val newProductId = ProductDao.insert("Телефон", "Смартфон", 599.99)
     * Добавляем товар "Телефон", "Смартфон", цена 599.99.
     * Метод вернет ID нового товара.
     */
    fun insert(name: String, description: String, price: Double): Int = transaction {
        Products.insert {
            it[Products.name] = name
            it[Products.description] = description
            it[Products.price] = price.toBigDecimal()
        } get Products.id
    }

    /**
     * Удаление товара
     *
     * Метод принимает id: Int – ID товара для удаления.
     * Products.deleteWhere { Products.id eq id }
     * Выполняем SQL-запрос:
     * DELETE FROM products WHERE id = ?
     * Удаляем товар по ID.
     * Как использовать?
     * ProductDao.delete(3)
     * Удалим товар с ID = 3.
     */
    fun delete(id: Int) = transaction {
        Products.deleteWhere { Products.id eq id }
    }

    /**
     * Удаление товара с очисткой связей
     *
     * Метод принимает id: Int – ID товара для удаления.
     * ProductSuppliers.deleteWhere { ProductSuppliers.productId eq id }
     *
     * Сначала удаляем связи товара с поставщиками.
     * Products.deleteWhere { Products.id eq id }
     *
     * Затем удаляем сам товар.
     * Метод:
     * Удаляет товар и очищает его связи с поставщиками.
     *
     * Как использовать?
     * ProductDao.delete2(4)
     * Удалит товар с ID = 4.
     * Очистит все связи с поставщиками.
     */
    fun deleteWithSuppliers(id: Int) = transaction {
        // Удаляем связь товара с поставщиками
        ProductSuppliers.deleteWhere { ProductSuppliers.productId eq id }
        Products.deleteWhere { Products.id eq id }
    }

    /**
     * обновление товара (название, цена ....)
     *
     * Метод принимает ID товара и новые значения (название, цена ...).
     * Обновляет товар в базе данных.
     * transaction { Открываем транзакцию, чтобы все изменения были атомарными.
     * Products.update({ Products.id eq id }) {
     *
     * Выполняем SQL-запрос UPDATE products SET ... WHERE id = ?.
     * it[Products.name] = name
     *
     * Устанавливаем новое название.
     * it[Products.description] = description
     *
     * Устанавливаем новое описание.
     * it[Products.price] = price.toBigDecimal()
     *
     * Конвертируем Double → BigDecimal, чтобы сохранить в БД.
     * Как использовать метод update()?
     * ProductDao.update(2, "Ноутбук", "Игровой ноутбук с RTX 4080", 2999.99)
     * Найдет товар с ID = 2.
     * Изменит его название на "Ноутбук".
     * Обновит описание на "Игровой ноутбук с RTX 4080".
     * Поставит новую цену 2999.99.
     */
    fun update(id: Int, name: String, description: String, price: BigDecimal) = transaction {
        Products.update({ Products.id eq id }) {
            it[Products.name] = name
            it[Products.description] = description
            it[Products.price] = price
        }
    }

    /**
     * Получить поставщиков товара
     *
     * Метод принимает ID товара (productId).
     * Возвращает список поставщиков (List<ResultRow>).
     * = transaction { Открываем транзакцию, чтобы гарантировать атомарность.
     * ProductSuppliers.innerJoin(Counterparties)
     *
     * Объединяем таблицы product_suppliers и counterparties по supplier_id.
     * Это аналог SQL-запроса:
     * SELECT * FROM product_suppliers
     * INNER JOIN counterparties ON product_suppliers.supplier_id = counterparties.id
     * WHERE product_suppliers.product_id = ?;
     * .select { ProductSuppliers.productId eq productId }
     *
     * Выбираем только те записи, которые относятся к переданному productId.
     * .toList()
     *
     * Преобразуем результат в список поставщиков (List<ResultRow>).
     * Как использовать метод getSuppliersByProduct()?
     * val suppliers = ProductDao.getSuppliersByProduct(5)
     * Найдет всех поставщиков товара с ID = 5.
     * Вернет список записей из counterparties.
     */
    fun getSuppliersByProduct(productId: Int): List<ResultRow> = transaction {
        ProductSuppliers
            .innerJoin(Counterparties)
            .select { ProductSuppliers.productId eq productId }
            .toList()
    }
}