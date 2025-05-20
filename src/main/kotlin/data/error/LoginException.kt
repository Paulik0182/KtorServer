package com.example.data.error

class LoginException(
    val code: String,
    override val message: String
) : Exception(message)
