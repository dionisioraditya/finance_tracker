package com.example.financetracker.data.repository

import com.example.financetracker.core.database.FinanceDatabase
import com.example.financetracker.core.datastore.UserPreferences
import com.example.financetracker.data.remote.api.ApiClient
import com.example.financetracker.data.remote.dto.LoginRequest
import com.example.financetracker.data.remote.dto.RegisterRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AuthRepository(
    private val apiClient: ApiClient,
    private val userPreferences: UserPreferences,
    private val database: FinanceDatabase
) {
    val isLoggedIn: Flow<Boolean> = userPreferences.authToken.map { !it.isNullOrBlank() }
    val userName: Flow<String?> = userPreferences.userName
    val userEmail: Flow<String?> = userPreferences.userEmail
    val userId: Flow<Long?> = userPreferences.userId

    suspend fun login(email: String, pass: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiClient.login(LoginRequest(email.trim(), pass)).getOrThrow()
            val token = response.token ?: throw IllegalStateException("Token not provided in response")
            val user = response.user ?: throw IllegalStateException("User profile missing")

            userPreferences.saveAuthSession(token, user.id, user.name, user.email)
        }
    }

    suspend fun register(name: String, email: String, pass: String, passConfirmation: String? = null): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiClient.register(RegisterRequest(name.trim(), email.trim(), pass, passConfirmation ?: pass)).getOrThrow()
            val token = response.token ?: throw IllegalStateException("Token not provided in response")
            val user = response.user ?: throw IllegalStateException("User profile missing")

            userPreferences.saveAuthSession(token, user.id, user.name, user.email)
        }
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        userPreferences.clearSession()
        database.clearAllTables()
    }
}
