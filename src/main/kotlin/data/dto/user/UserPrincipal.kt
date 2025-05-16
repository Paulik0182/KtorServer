package com.example.data.dto.user

data class UserPrincipal(
    val userId: Long,
    val role: String
) : io.ktor.server.auth.Principal
