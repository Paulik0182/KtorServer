package com.example.data

import com.example.Users
import com.example.data.error.AuthException
import com.example.data.error.AuthValidation.emailExists
import com.example.data.error.AuthValidation.isValidEmail
import com.example.data.error.AuthValidation.isValidPassword
import com.example.data.error.LoginException
import com.example.routing.UserRole
import com.example.utils.Days.formatDaysLeft
import org.mindrot.jbcrypt.BCrypt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

object UserDao {

    fun register(email: String, password: String, role: UserRole = UserRole.CUSTOMER, counterpartyId: Long?): Long {
        return transaction {
            if (!isValidEmail(email)) {
                throw AuthException("invalid_email", "Некорректный email")
            }

            if (emailExists(email)) {
                throw AuthException("email_exists", "Пользователь с таким email уже существует")
            }

            // По сути это не будет вызываться если пользователь уже существует. Но если сделать проверку наличия
            // пользователя потом, то получается что можно будет проверять есть ли такой пользователь в системе или
            // нет, это некорректно, получается что посторонний может выяснять состояние и статус пользователя в системе.
            val row = Users.selectAll().where { Users.email eq email }.firstOrNull()
            if (row != null && row[Users.isBlocked]) {
                val (code, msg) = getBlockedReason(row)
                throw AuthException(code, msg)
            }

            if (!isValidPassword(password)) {
                throw AuthException(
                    "weak_password",
                    "Пароль должен содержать минимум 6 символов, одну заглавную букву и цифру"
                )
            }

            val counterpartyIdFinal = counterpartyId ?: CounterpartyDao.insertDefaultCounterparty(email)
            val hash = BCrypt.hashpw(password, BCrypt.gensalt())

            Users.insert {
                it[Users.email] = email
                it[Users.hashedPassword] = hash
                it[Users.role] = role
                it[Users.counterpartyId] = counterpartyIdFinal
            } get Users.id
        }
    }

    fun validateLogin(email: String, password: String): Triple<Long, UserRole, Long?> {
        return transaction {
            val row = Users
                .selectAll()
                .where { Users.email eq email }
                .firstOrNull() ?: throw LoginException("wrong_credentials", "Неверный логин или пароль")

            if (row[Users.isBlocked]) {
                val (code, msg) = getBlockedReason(row)
                throw LoginException(code, msg)
            }

            val valid = BCrypt.checkpw(password, row[Users.hashedPassword])
            if (!valid) throw LoginException("wrong_credentials", "Неверный логин или пароль")

            Triple(row[Users.id], row[Users.role], row[Users.counterpartyId])
        }
    }

    fun requestPasswordReset(email: String): String {
        return transaction {
            if (!isValidEmail(email)) {
                throw AuthException("invalid_email", "Некорректный email")
            }

            val row = Users.selectAll().where { Users.email eq email }.firstOrNull()
                ?: throw AuthException("email_not_found", "Пользователь не найден")

            if (row[Users.isBlocked]) {
                val (code, msg) = getBlockedReason(row)
                throw AuthException(code, msg)
            }

            val token = UUID.randomUUID().toString()
            PasswordTokenDao.storeToken(row[Users.id], token, Duration.ofMinutes(15))
            token
        }
    }

    fun resetPassword(token: String, newPassword: String) {
        return transaction {
            if (!isValidPassword(newPassword)) {
                throw AuthException("weak_password", "Пароль должен содержать минимум 6 символов, одну заглавную букву и цифру")
            }

            val userId = PasswordTokenDao.validateAndConsumeToken(token)
                ?: throw AuthException("invalid_token", "Неверный или просроченный токен")

            updatePassword(userId, newPassword)
        }
    }

    fun exists(userId: Long): Boolean {
        return transaction {
            Users.selectAll().where { Users.id eq userId }.count() > 0
        }
    }

    // Для сброса пароля
    fun updatePassword(userId: Long, newPassword: String) {
        val hash = BCrypt.hashpw(newPassword, BCrypt.gensalt())
        transaction {
            Users.update({ Users.id eq userId }) {
                it[hashedPassword] = hash
            }
        }
    }

    fun findUserIdByEmail(email: String): Long? {
        return transaction {
            Users
                .selectAll().where { Users.email eq email }
                .map { it[Users.id] }
                .singleOrNull()
        }
    }

    fun findUserByEmail(email: String): ResultRow? {
        return transaction {
            Users.selectAll().where { Users.email eq email }.firstOrNull()
        }
    }

    // Блокировка пользователя (условно удалили)
    fun markUserAsDeleted(userId: Long) {
        transaction {
            Users.update({ Users.id eq userId }) {
                it[isBlocked] = true
                it[blockedByAdmin] = false
                it[blockedAt] = LocalDateTime.now()
                it[blockComment] = "Пользователь сам удалил аккаунт"
            }
        }
    }

    // Разблокировка
    fun unblockUser(userId: Long) {
        transaction {
            Users.update({ Users.id eq userId }) {
                it[isBlocked] = false
                it[blockedByAdmin] = false
            }
        }
    }

    fun blockUserByAdmin(userId: Long, comment: String?) {
        transaction {
            val role = Users
                .select(Users.role)
                .where { Users.id eq userId }
                .map { it[Users.role] }
                .firstOrNull()

            if (role?.name == UserRole.SYSTEM_ADMIN.name) {
                error("Нельзя заблокировать администратора")
            }

            Users.update({ Users.id eq userId }) {
                it[isBlocked] = true
                it[blockedByAdmin] = true
                it[blockedAt] = LocalDateTime.now()
                it[blockComment] = comment ?: "Заблокировано администратором"
            }
        }
    }

    private fun getBlockedReason(row: ResultRow): Pair<String, String> {
        val blockedByAdmin = row[Users.blockedByAdmin]
        val blockedAt = row[Users.blockedAt]

        return if (blockedByAdmin) {
            "user_blocked_admin" to "Вы заблокированы, обратитесь в поддержку"
        } else {
            val daysSinceBlock = blockedAt?.let { Duration.between(it, LocalDateTime.now()).toDays() } ?: 0
            val daysLeft = 30 - daysSinceBlock

            if (daysSinceBlock > 30) {
                "user_deleted_self" to "Ваш аккаунт был удалён. Обратитесь в поддержку"
            } else {
                "user_soft_deleted" to "Пользователь подал заявку на удаление.\nАккаунт будет удален через ${formatDaysLeft(daysLeft)}, за восстановлением обратитесь в поддержку"
            }
        }
    }
}
