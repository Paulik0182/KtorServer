package com.example.data

import com.example.Counterparties
import com.example.ProductSuppliers
import com.example.data.ProductDao.getCounterpartyName
import com.example.data.ProductDao.getProductName
import com.example.data.dto.product.ProductSupplierResponse
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Этот объект управляет связями между товарами и поставщиками.
 * Он позволяет:
 *
 * Получить поставщиков для конкретного товара.
 * Добавить поставщика к товару.
 * Удалить поставщика от товара.
 */
object ProductSupplierDao {

    fun getProductSuppliersForCounterparty(id: Long): List<ProductSupplierResponse> = transaction {
        ProductSuppliers
            .selectAll()
            .where { ProductSuppliers.counterpartyId eq id }
            .map {
                ProductSupplierResponse(
                    id = it[ProductSuppliers.id],
                    productId = it[ProductSuppliers.productId],
                    productName = ProductDao.getProductName(it[ProductSuppliers.productId]),
                    counterpartyId = it[ProductSuppliers.counterpartyId],
                    counterpartyName = ProductDao.getCounterpartyName(it[ProductSuppliers.counterpartyId])
                )
            }
    }

    // Получение поставщиков товара
    fun getProductSuppliers(productId: Long): List<ProductSupplierResponse> = transaction {
        ProductSuppliers
            .innerJoin(Counterparties)
            .selectAll().where { ProductSuppliers.productId eq productId }
            .map {
                ProductSupplierResponse(
                    id = it[ProductSuppliers.id],
                    productId = it[ProductSuppliers.productId],
                    productName = getProductName(it[ProductSuppliers.productId]),
                    counterpartyId = it[ProductSuppliers.counterpartyId],
                    counterpartyName = getCounterpartyName(it[ProductSuppliers.counterpartyId])
                )
            }
    }

    /**
     * Получение списка поставщиков для товара
     *
     * Метод принимает productId — ID товара.
     * Возвращает список поставщиков (List<ResultRow>).
     * = transaction { Открываем транзакцию.
     * ProductSuppliers.innerJoin(Counterparties)
     *
     * Объединяем таблицы product_suppliers и counterparties по supplier_id.
     * Это аналог SQL-запроса:
     * SELECT * FROM product_suppliers
     * INNER JOIN counterparties ON product_suppliers.supplier_id = counterparties.id
     * WHERE product_suppliers.product_id = ?;
     * .select { ProductSuppliers.productId eq productId }
     *
     * Выбираем только записи, относящиеся к переданному productId.
     * .toList()
     *
     * Преобразуем результат в список.
     * Метод Возвращает список поставщиков, связанных с товаром.
     *
     * Как использовать?
     * val suppliers = ProductSupplierDao.getSuppliersByProduct(3)
     * Найдет всех поставщиков товара с ID = 3.
     */
//    fun getSuppliersByProduct(productId: Int): List<ResultRow> = transaction {
//        ProductSuppliers
//            .innerJoin(Counterparties)
//            .selectAll().where { ProductSuppliers.productId eq productId }
//            .toList()
//    }

    /**
     * Добавление связи товара и поставщика
     *
     * Метод принимает:
     * productId — ID товара.
     * supplierId — ID поставщика.
     * = transaction { Открываем транзакцию.
     * ProductSuppliers.insert {
     *
     * Выполняем SQL-запрос:
     * INSERT INTO product_suppliers (product_id, supplier_id) VALUES (?, ?);
     * it[ProductSuppliers.productId] = productId
     *
     * Вставляем ID товара.
     * it[ProductSuppliers.counterpartyId] = supplierId
     *
     * Вставляем ID поставщика.
     * Метод создает связь между товаром и поставщиком.
     *
     * Как использовать?
     * ProductSupplierDao.insert(3, 5)
     * Товар с ID = 3 теперь связан с поставщиком с ID = 5.
     */
//    fun addSupplierToProduct(productId: Int, supplierId: Int) = transaction {
//        ProductSuppliers.insert {
//            it[ProductSuppliers.productId] = productId
//            it[ProductSuppliers.supplierId] = supplierId
//        }
//    }

    /**
     * Удаление связи товара и поставщика
     *
     * Метод принимает:
     * productId — ID товара.
     * supplierId — ID поставщика.
     * = transaction { Открываем транзакцию.
     * ProductSuppliers.deleteWhere {
     *
     * Выполняем SQL-запрос:
     * DELETE FROM product_suppliers
     * WHERE product_id = ? AND supplier_id = ?;
     * (ProductSuppliers.productId eq productId) and (ProductSuppliers.supplierId eq supplierId)
     *
     * Удаляем только конкретное соответствие товара и поставщика.
     * Метод удаляет связь между товаром и поставщиком.
     *
     * Как использовать?
     * ProductSupplierDao.removeSupplierFromProduct(3, 5)
     * Теперь товар с ID = 3 больше не привязан к поставщику с ID = 5.
     */
//    fun removeSupplierFromProduct(productId: Int, supplierId: Int) = transaction {
//        ProductSuppliers.deleteWhere {
//            (ProductSuppliers.productId eq productId) and (ProductSuppliers.supplierId eq supplierId)
//        }
//    }
}
