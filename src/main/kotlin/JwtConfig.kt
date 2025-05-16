package com.example

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val secret = "your-super-secret-key" // используй UUID или сложную строку
    private const val issuer = "your-app-name"
    private const val validityInMs = 1000 * 60 * 60 * 24 // 24 часа

    private val algorithm = Algorithm.HMAC512(secret)

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun makeToken(userId: Long, role: String, rawToken: String): String {
        val now = System.currentTimeMillis()
        return JWT.create()
            .withIssuer(issuer)
            .withClaim("userId", userId)
            .withClaim("role", role)
            .withClaim("token", rawToken) // добавляем токен внутрь как обычный claim
            .withExpiresAt(Date(now + validityInMs))
            .sign(algorithm)
    }
}
