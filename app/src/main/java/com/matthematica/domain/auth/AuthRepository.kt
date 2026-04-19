package com.matthematica.domain.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.matthematica.data.database.dao.UserDao
import com.matthematica.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.random.Random

@Singleton
class AuthRepository @Inject constructor(
    private val userDao: UserDao,
    private val dataStore: DataStore<Preferences>
) {
    private val userIdKey = stringPreferencesKey("user_id")
    private val emailKey = stringPreferencesKey("email")

    val currentUserId: Flow<String?> = dataStore.data.map { it[userIdKey] }
    val currentEmail: Flow<String?> = dataStore.data.map { it[emailKey] }

    suspend fun signup(email: String, password: String, displayName: String): Result<User> {
        return try {
            // Validate inputs
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
            }

            // Check if user exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(IllegalArgumentException("User already exists"))
            }

            // Create new user
            val userId = UUID.randomUUID().toString()
            val passwordHash = hashPassword(password)
            val newUser = User(
                id = userId,
                email = email,
                passwordHash = passwordHash,
                displayName = displayName.ifBlank { email.substringBefore("@") }
            )

            userDao.insertUser(newUser)
            saveSession(userId, email)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            if (email.isBlank() || password.isBlank()) {
                return Result.failure(IllegalArgumentException("Email and password cannot be empty"))
            }

            val user = userDao.getUserByEmail(email)
            if (user == null) {
                return Result.failure(IllegalArgumentException("User not found"))
            }

            if (!verifyPassword(password, user.passwordHash)) {
                return Result.failure(IllegalArgumentException("Invalid password"))
            }

            saveSession(user.id, user.email)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        dataStore.edit { preferences ->
            preferences.remove(userIdKey)
            preferences.remove(emailKey)
        }
    }

    suspend fun getCurrentUser(userId: String): User? {
        return userDao.getUserById(userId)
    }

    private suspend fun saveSession(userId: String, email: String) {
        dataStore.edit { preferences ->
            preferences[userIdKey] = userId
            preferences[emailKey] = email
        }
    }

    private fun hashPassword(password: String): String {
        // In production, use BCrypt or similar
        // For now, use simple hash
        return password.hashCode().toString()
    }

    private fun verifyPassword(password: String, hash: String): Boolean {
        // In production, use BCrypt verification
        return hashPassword(password) == hash
    }
}

