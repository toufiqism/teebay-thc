package com.teebay.appname.data.local.entities

import kotlinx.serialization.Serializable

@Serializable
data class UserEntity(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String, // Simple string for now, as per requirements
    val phoneNumber: String,
    val address: String,
    val dateOfBirth: String,
    val biometricEnabled: Boolean = false
) 