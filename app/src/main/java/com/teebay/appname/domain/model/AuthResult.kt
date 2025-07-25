package com.teebay.appname.domain.model

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object BiometricRequired : AuthResult()
    object BiometricSuccess : AuthResult()
    object BiometricError : AuthResult()
}

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phoneNumber: String,
    val address: String,
    val dateOfBirth: String
) 