package com.example

import kotlinx.serialization.json.Json
import kotlinx.serialization.KSerializer
import org.jetbrains.exposed.sql.ColumnType
import org.postgresql.util.PGobject

/**
 * TODO Не используется!!!
 * JsonColumnType<T> — это пользовательский тип колонки для Exposed, который позволяет сохранять объекты Kotlin в базе
 * данных PostgreSQL в формате JSONB и автоматически сериализовать/десериализовать их с использованием kotlinx.serialization.
 * Этот класс расширяет ColumnType<T> и предоставляет корректное преобразование значений из базы данных в объекты Kotlin и обратно.
 *
 *При записи в базу данных:
 * - Объект Kotlin (T) преобразуется в строку JSON с помощью Json.encodeToString().
 * Записывается в поле типа JSONB в PostgreSQL.
 * - При чтении из базы данных:
 * Если из базы пришла строка JSON, она десериализуется обратно в объект T с помощью Json.decodeFromString().
 * - Дополнительное преобразование:
 * Для работы с SQL-запросами значения приводятся к строковому виду через nonNullValueToString().
 *
 * T : Any — обобщенный тип, который должен быть @Serializable, так как используется kotlinx.serialization.
 * clazz: KClass<T> — передается KClass<T>, так как Exposed использует рефлексию для определения сериализатора.
 */
class JsonColumnType<T : Any>(private val serializer: KSerializer<T>) : ColumnType<T>() {

    /**
     * Возвращает тип данных JSONB, который используется в PostgreSQL.
     */
    override fun sqlType(): String = "JSONB" // Используем JSONB для PostgreSQL

    /**
     * Принимает: value: Any (значение из базы данных).
     * Возвращает: Десериализованный объект T.
     * Логика:
     * Если значение — это String, десериализует его из JSON в объект T.
     * Если пришел другой тип, выбрасывает IllegalArgumentException.
     */
    override fun valueFromDB(value: Any): T {
        val jsonString = when (value) {
            is String -> value
            is PGobject -> value.value ?: ""
            else -> throw IllegalArgumentException("Unsupported value: $value")
        }
        return Json.decodeFromString(serializer, jsonString)
    }

    /**
     * Принимает: value: T (объект Kotlin).
     * Возвращает: JSON-строку, готовую для сохранения в базу.
     * Логика:
     * Преобразует объект T в JSON, который будет записан в PostgreSQL.
     */
    override fun notNullValueToDB(value: T): Any {
        val pg = PGobject()
        pg.type = "jsonb"
        pg.value = Json.encodeToString(serializer, value)
        return pg
    }

    /**
     * Принимает: value: T (объект Kotlin).
     * Возвращает: JSON-строку в SQL-совместимом формате.
     * Логика:
     * Кодирует объект T в JSON.
     * Оборачивает его в одиночные кавычки, так как PostgreSQL требует этого в SQL-запросах.
     */
    override fun nonNullValueToString(value: T): String {
        return "'${Json.encodeToString(serializer, value)}'" // SQL-совместимый формат
    }
}
