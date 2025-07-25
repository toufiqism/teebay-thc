package com.teebay.appname.data.mappers

import com.teebay.appname.data.local.entities.UserEntity
import com.teebay.appname.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        phoneNumber = phoneNumber,
        address = address,
        dateOfBirth = dateOfBirth,
        biometricEnabled = biometricEnabled
    )
}

fun User.toEntity(password: String): UserEntity {
    return UserEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        email = email,
        password = password,
        phoneNumber = phoneNumber,
        address = address,
        dateOfBirth = dateOfBirth,
        biometricEnabled = biometricEnabled
    )
} 