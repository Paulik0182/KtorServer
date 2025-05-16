package com.example.data

import com.example.UserSessions
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object UserSessionDao {
    fun addSession(userId: Long, token: String, expiresAt: Long, deviceInfo: String?) {
        val localDateTime = Instant.ofEpochMilli(expiresAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        transaction {
            UserSessions.insert {
                it[UserSessions.userId] = userId
                it[UserSessions.token] = token
                it[UserSessions.expiresAt] = localDateTime
                it[UserSessions.deviceInfo] = deviceInfo
            }
        }
    }

    fun isValidToken(userId: Long, token: String): Boolean {
        return transaction {
            UserSessions.selectAll().where {
                (UserSessions.userId eq userId) and (UserSessions.token eq token)
            }.firstOrNull() != null
        }
    }

    fun deleteToken(token: String) {
        transaction {
            UserSessions.deleteWhere { UserSessions.token eq token }
        }
    }
}
