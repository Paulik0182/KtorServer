package com.example.data

import com.example.PasswordRecoveryTokens
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object PasswordTokenDao {

    fun storeToken(userId: Long, token: String, expiration: Duration = Duration.ofMinutes(15)) {
        val expiresAt = Instant.now().plus(expiration).atZone(ZoneId.systemDefault()).toLocalDateTime()

        transaction {
            // Удалим старые токены перед вставкой
            PasswordRecoveryTokens.deleteWhere { PasswordRecoveryTokens.userId eq userId }

            PasswordRecoveryTokens.insert {
                it[PasswordRecoveryTokens.userId] = userId
                it[PasswordRecoveryTokens.token] = token
                it[PasswordRecoveryTokens.expiresAt] = expiresAt
            }
        }
    }

    fun validateAndConsumeToken(token: String): Long? {
        return transaction {
            val now = LocalDateTime.now()
            val row = PasswordRecoveryTokens
                .selectAll().where { (PasswordRecoveryTokens.token eq token) and (PasswordRecoveryTokens.expiresAt greater now) }
                .firstOrNull()

            if (row != null) {
                // Удалим токен после использования
                PasswordRecoveryTokens.deleteWhere { PasswordRecoveryTokens.token eq token }
                row[PasswordRecoveryTokens.userId]
            } else {
                null
            }
        }
    }

    // Заглушка TODO заменить когда подключим SMTP, например JavaMail
    fun sendEmail(to: String, content: String) {
        println("== Письмо отправлено на $to ==")
        println(content)
    }
}
