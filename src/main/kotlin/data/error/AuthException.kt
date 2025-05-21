package com.example.data.error

class AuthException(val code: String, override val message: String) : RuntimeException(message)
