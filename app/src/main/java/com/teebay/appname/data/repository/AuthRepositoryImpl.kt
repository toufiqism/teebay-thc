package com.teebay.appname.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teebay.appname.data.local.entities.UserEntity
import com.teebay.appname.data.mappers.toDomain
import com.teebay.appname.data.mappers.toEntity
import com.teebay.appname.domain.model.AuthResult
import com.teebay.appname.domain.model.LoginRequest
import com.teebay.appname.domain.model.RegisterRequest
import com.teebay.appname.domain.model.User
import com.teebay.appname.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class AuthRepositoryImpl(private val context: Context) : AuthRepository {

    private object PreferencesKeys {
        val CURRENT_USER = stringPreferencesKey("current_user")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USERS_DATA = stringPreferencesKey("users_data")
    }

    override suspend fun login(request: LoginRequest): AuthResult {
        ensureDefaultUser()
        return try {
            val users = getStoredUsers()
            val user = users.find { it.email == request.email && it.password == request.password }
            
            if (user != null) {
                saveCurrentUser(user.toDomain())
                AuthResult.Success(user.toDomain())
            } else {
                AuthResult.Error("Invalid email or password")
            }
        } catch (e: Exception) {
            AuthResult.Error("Login failed: ${e.message}")
        }
    }

    override suspend fun register(request: RegisterRequest): AuthResult {
        ensureDefaultUser()
        return try {
            if (request.password != request.confirmPassword) {
                return AuthResult.Error("Passwords do not match")
            }

            val users = getStoredUsers().toMutableList()
            
            // Check if user already exists
            if (users.any { it.email == request.email }) {
                return AuthResult.Error("User with this email already exists")
            }

            val newUser = UserEntity(
                id = UUID.randomUUID().toString(),
                firstName = request.firstName,
                lastName = request.lastName,
                email = request.email,
                password = request.password,
                phoneNumber = request.phoneNumber,
                address = request.address,
                dateOfBirth = request.dateOfBirth,
                biometricEnabled = false
            )

            users.add(newUser)
            saveUsers(users)
            saveCurrentUser(newUser.toDomain())
            
            AuthResult.Success(newUser.toDomain())
        } catch (e: Exception) {
            AuthResult.Error("Registration failed: ${e.message}")
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val userJson = context.dataStore.data.first()[PreferencesKeys.CURRENT_USER]
            userJson?.let { Json.decodeFromString<User>(it) }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_USER] = ""
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
        }
    }

    override suspend fun enableBiometric(userId: String) {
        updateUserBiometric(userId, true)
    }

    override suspend fun disableBiometric(userId: String) {
        updateUserBiometric(userId, false)
    }

    override fun isUserLoggedIn(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }
    }

    private suspend fun saveCurrentUser(user: User) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENT_USER] = Json.encodeToString(user)
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
        }
    }

    private suspend fun getStoredUsers(): List<UserEntity> {
        return try {
            val usersJson = context.dataStore.data.first()[PreferencesKeys.USERS_DATA]
            if (usersJson.isNullOrEmpty()) {
                emptyList()
            } else {
                Json.decodeFromString<List<UserEntity>>(usersJson)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun saveUsers(users: List<UserEntity>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USERS_DATA] = Json.encodeToString(users)
        }
    }

    private suspend fun updateUserBiometric(userId: String, enabled: Boolean) {
        val users = getStoredUsers().toMutableList()
        val userIndex = users.indexOfFirst { it.id == userId }
        
        if (userIndex != -1) {
            users[userIndex] = users[userIndex].copy(biometricEnabled = enabled)
            saveUsers(users)
            
            // Update current user if it's the same user
            val currentUser = getCurrentUser()
            if (currentUser?.id == userId) {
                saveCurrentUser(currentUser.copy(biometricEnabled = enabled))
            }
        }
    }

    private suspend fun ensureDefaultUser() {
        val users = getStoredUsers().toMutableList()
        if (users.none { it.email == "a@a.com" }) {
            val defaultUser = com.teebay.appname.data.local.entities.UserEntity(
                id = java.util.UUID.randomUUID().toString(),
                firstName = "Default",
                lastName = "User",
                email = "a@a.com",
                password = "aA111111",
                phoneNumber = "0000000000",
                address = "Default Address",
                dateOfBirth = "1990-01-01",
                biometricEnabled = true
            )
            users.add(defaultUser)
            saveUsers(users)
        }
    }
} 