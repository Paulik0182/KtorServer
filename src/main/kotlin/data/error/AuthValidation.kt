package com.example.data.error

import com.example.Users
import org.jetbrains.exposed.sql.selectAll

object AuthValidation {

    fun isValidEmail(email: String): Boolean =
        email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))

    fun isValidPassword(password: String): Boolean =
        password.length >= 6 && password.any { it.isUpperCase() } && password.any { it.isDigit() }

    fun emailExists(email: String): Boolean =
        Users.selectAll().where { Users.email eq email }.count() > 0
}
