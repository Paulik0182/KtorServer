package com.example.data

import com.example.Users
import com.example.routing.UserRole
import org.mindrot.jbcrypt.BCrypt
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

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

    fun validateLogin(email: String, password: String): Pair<Long, UserRole> {
        return transaction {
            val row = Users.selectAll().where { Users.email eq email }.firstOrNull()
                ?: error("Неверный логин или пароль")

            if (!BCrypt.checkpw(password, row[Users.hashedPassword])) {
                error("Неверный логин или пароль")
            }

            row[Users.id] to row[Users.role]
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
}
