package com.teebay.appname.domain.repository

import com.teebay.appname.domain.model.AuthResult
import com.teebay.appname.domain.model.LoginRequest
import com.teebay.appname.domain.model.RegisterRequest
import com.teebay.appname.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(request: LoginRequest): AuthResult
    suspend fun register(request: RegisterRequest): AuthResult
    suspend fun getCurrentUser(): User?
    suspend fun logout()
    suspend fun enableBiometric(userId: String)
    suspend fun disableBiometric(userId: String)
    fun isUserLoggedIn(): Flow<Boolean>
} 