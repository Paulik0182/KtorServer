package com.example.data

import com.example.Users
import com.example.routing.UserRole
import org.mindrot.jbcrypt.BCrypt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Duration
import java.time.LocalDateTime

object UserDao {

    fun register(email: String, password: String, role: UserRole = UserRole.CUSTOMER, counterpartyId: Long?): Long {
        return transaction {
            val existing = Users.selectAll().where { Users.email eq email }.firstOrNull()
            if (existing != null) error("Пользователь с таким email уже существует")

            val hash = BCrypt.hashpw(password, BCrypt.gensalt())

            Users.insert {
                it[Users.email] = email
                it[Users.hashedPassword] = hash
                it[Users.role] = role
                it[Users.counterpartyId] = counterpartyId
            } get Users.id
        }
    }

    fun validateLogin(email: String, password: String): Triple<Long, UserRole, Long?> {
        return transaction {
            val row = Users
                .selectAll()
                .where { Users.email eq email }
                .firstOrNull() ?: error("Неверный логин или пароль")

            if (row[Users.isBlocked]) {
                val blockedByAdmin = row[Users.blockedByAdmin]
                val blockedAt = row[Users.blockedAt]
                val daysSinceBlock = blockedAt?.let { Duration.between(it, LocalDateTime.now()).toDays() } ?: 0

                val msg = when {
                    blockedByAdmin -> "Вы заблокированы, обратитесь в поддержку"
                    daysSinceBlock > 30 -> "Ранее вы имели аккаунт но удалили его, обратитесь в поддержку" // TODO Нужно придумать получше сценарий
                    else -> "Пользователь будет удален через 30 дней, за восстановление аккаунта можно обратиться к администратору"
                }

                error(msg)
            }

            val valid = BCrypt.checkpw(password, row[Users.hashedPassword])
            if (!valid) error("Неверный логин или пароль")

            Triple(row[Users.id], row[Users.role], row[Users.counterpartyId])
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
}
