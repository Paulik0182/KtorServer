package com.example.data

import com.example.UserSessions
import com.example.data.dto.user.UserSessionDto
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

object UserSessionDao {

    /**
     * Создает новую сессию, при этом удаляет старую с тем же deviceInfo для данного пользователя.
     */
    fun addSession(userId: Long, token: String, expiresAt: Long, deviceInfo: String?) {
        val localDateTime = Instant.ofEpochMilli(expiresAt)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()

        transaction {
            // Удаляем старую сессию с тем же устройством
            if (deviceInfo != null) {
                UserSessions.deleteWhere {
                    (UserSessions.userId eq userId) and (UserSessions.deviceInfo eq deviceInfo)
                }
            }

            UserSessions.insert {
                it[UserSessions.userId] = userId
                it[UserSessions.token] = token
                it[UserSessions.expiresAt] = localDateTime
                it[UserSessions.deviceInfo] = deviceInfo
            }
        }
    }

    /**
     * Проверяет, действителен ли токен и удаляет истёкшие сессии.
     */
    fun isValidToken(userId: Long, token: String): Boolean {
        return transaction {
            // Удаляем протухшие сессии перед проверкой
            UserSessions.deleteWhere { UserSessions.expiresAt less LocalDateTime.now() }

            UserSessions.selectAll().where {
                (UserSessions.userId eq userId) and (UserSessions.token eq token)
            }.firstOrNull() != null
        }
    }

    /**
     * Удаляет конкретный токен сессии (logout).
     */
    fun deleteToken(token: String) {
        transaction {
            UserSessions.deleteWhere { UserSessions.token eq token }
        }
    }

    /**
     * Возвращает все сессии (только для администратора).
     */
    fun getAllSessions(): List<UserSessionDto> {
        return transaction {
            UserSessions.selectAll().map {
                UserSessionDto(
                    userId = it[UserSessions.userId],
                    token = it[UserSessions.token],
                    createdAt = it[UserSessions.createdAt].toString(),
                    expiresAt = it[UserSessions.expiresAt].toString(),
                    deviceInfo = it[UserSessions.deviceInfo]
                )
            }
        }
    }

    /**
     * Удаляет все сессии пользователя. Используется при глобальном выходе из всех устройств.
     * Делает сам пользователь
     */
    fun deleteAllSessionsForUser(userId: Long) {
        transaction {
            UserSessions.deleteWhere { UserSessions.userId eq userId }
        }
    }

    /**
     * Удаляет все сессии пользователя, кроме текущей. Используется при logout_others.
     */
    fun deleteAllSessionsExcept(userId: Long, exceptToken: String) {
        transaction {
            UserSessions.deleteWhere {
                (UserSessions.userId eq userId) and (UserSessions.token neq exceptToken)
            }
        }
    }

    /**
     * Удаляет неактивные сессии старше 30 дней (idle timeout).
     * Вызывать при старте приложения или по расписанию.
     *
     * На перспективу, требуется обдумать
     * В main() или при старте приложения:
     * UserSessionDao.deleteExpiredIdleSessions()
     * 2. (опционально) Добавить планировщик (если нужна регулярная очистка):
     * Если использовать Ktor+Kotlin без Quartz/Redis, можно:
     * Локально: через Timer().schedule(...).
     *
     * На сервере: с помощью внешнего cron или системы задач.
     */
    fun deleteExpiredIdleSessions() {
        val threshold = LocalDateTime.now()
            .minusDays(30)
            .atZone(ZoneId.systemDefault())
            .toInstant()

        transaction {
            UserSessions.deleteWhere {
                UserSessions.createdAt less threshold
            }
        }
    }

    /**
     * Удаляет все сессии указанного пользователя (по userId). Используется админом.
     */
    fun deleteAllSessionsByAdmin(targetUserId: Long) {
        transaction {
            UserSessions.deleteWhere { UserSessions.userId eq targetUserId }
        }
    }

    /**
     * Удаляет все сессии, кроме сессии текущего администратора. Используется админом.
     */
    fun deleteAllSessionsExceptUser(adminUserId: Long) {
        transaction {
            UserSessions.deleteWhere { UserSessions.userId neq adminUserId }
        }
    }
}
