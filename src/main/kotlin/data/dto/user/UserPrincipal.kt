package com.example.data.dto.user

import com.example.routing.UserRole

data class UserPrincipal(
    val userId: Long,
    val role: UserRole
) : io.ktor.server.auth.Principal
