package com.teebay.appname.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phoneNumber: String,
    val address: String,
    val dateOfBirth: String,
    val biometricEnabled: Boolean = false
) 