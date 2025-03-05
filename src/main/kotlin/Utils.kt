package com.example

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

/**
 * TODO Не используется!!!
 * Функция jsonb<T>() — это расширение (extension function) для Exposed Table, которое добавляет поддержку JSONB-колонок в PostgreSQL.
 * Она позволяет создать колонку, которая автоматически сериализует и десериализует объекты Kotlin в JSON, используя JsonColumnType<T>.
 *
 * T : Any — обозначает, что T должен быть не-nullable (Any), то есть нельзя использовать T?.
 * reified — означает, что T не стирается (erased) во время компиляции, а остается доступным в рантайме.
 * Благодаря reified, мы можем передавать T::class, что позволяет Exposed корректно работать с JsonColumnType<T>.
 */
inline fun <reified T : Any> Table.jsonb(name: String): Column<T> {
    return registerColumn(name, JsonColumnType(T::class))
}

fun String.toList(): List<String> = Json.decodeFromString(this)