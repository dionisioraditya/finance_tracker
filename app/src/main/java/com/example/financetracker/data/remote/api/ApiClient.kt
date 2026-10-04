package com.example.financetracker.data.remote.api

import com.example.financetracker.BuildConfig
import com.example.financetracker.core.datastore.UserPreferences
import com.example.financetracker.data.remote.dto.AuthResponse
import com.example.financetracker.data.remote.dto.BudgetSummaryResponse
import com.example.financetracker.data.remote.dto.ColdWalletSummaryDto
import com.example.financetracker.data.remote.dto.LoginRequest
import com.example.financetracker.data.remote.dto.MonthlyBudgetDto
import com.example.financetracker.data.remote.dto.RegisterRequest
import com.example.financetracker.data.remote.dto.SyncRequest
import com.example.financetracker.data.remote.dto.SyncResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class ApiClient(private val userPreferences: UserPreferences) {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private fun getBaseUrl(): String {
        return BuildConfig.API_BASE_URL
    }

    private suspend fun getAuthToken(): String? {
        return userPreferences.authToken.firstOrNull()
    }

    private suspend fun makeRequest(requestBuilder: Request.Builder): okhttp3.Response {
        val token = getAuthToken()
        requestBuilder.addHeader("Accept", "application/json")
        requestBuilder.addHeader("Connection", "close")
        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        val request = requestBuilder.build()
        return withContext(Dispatchers.IO) {
            okHttpClient.newCall(request).execute()
        }
    }

    suspend fun login(loginRequest: LoginRequest): Result<AuthResponse> = runCatching {
        val baseUrl = getBaseUrl()
        val body = json.encodeToString(loginRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/login")
            .post(body)

        val response = makeRequest(request)
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw IOException(parseErrorMessage(response.code, responseBody, "Login"))
        }
        json.decodeFromString<AuthResponse>(responseBody)
    }

    suspend fun register(registerRequest: RegisterRequest): Result<AuthResponse> = runCatching {
        val baseUrl = getBaseUrl()
        val body = json.encodeToString(registerRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/auth/register")
            .post(body)

        val response = makeRequest(request)
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw IOException(parseErrorMessage(response.code, responseBody, "Pendaftaran"))
        }
        json.decodeFromString<AuthResponse>(responseBody)
    }

    suspend fun getBudgetSummary(month: String): Result<BudgetSummaryResponse> = runCatching {
        val baseUrl = getBaseUrl()
        val request = Request.Builder()
            .url("$baseUrl/finance/budgets/$month")
            .get()

        val response = makeRequest(request)
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw IOException(parseErrorMessage(response.code, responseBody, "Memuat anggaran"))
        }
        json.decodeFromString<BudgetSummaryResponse>(responseBody)
    }

    suspend fun saveBudget(budget: MonthlyBudgetDto): Result<BudgetSummaryResponse> = runCatching {
        val baseUrl = getBaseUrl()
        val body = json.encodeToString(budget).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/finance/budgets")
            .post(body)

        val response = makeRequest(request)
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw IOException(parseErrorMessage(response.code, responseBody, "Menyimpan anggaran"))
        }
        json.decodeFromString<BudgetSummaryResponse>(responseBody)
    }

    suspend fun getColdWalletSummary(): Result<ColdWalletSummaryDto> = runCatching {
        val baseUrl = getBaseUrl()
        val request = Request.Builder()
            .url("$baseUrl/finance/cold-wallet/summary")
            .get()

        val response = makeRequest(request)
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw IOException(parseErrorMessage(response.code, responseBody, "Memuat cold wallet"))
        }
        json.decodeFromString<ColdWalletSummaryDto>(responseBody)
    }

    suspend fun sync(syncRequest: SyncRequest): Result<SyncResponse> = runCatching {
        val baseUrl = getBaseUrl()
        val body = json.encodeToString(syncRequest).toRequestBody(jsonMediaType)
        val request = Request.Builder()
            .url("$baseUrl/finance/sync")
            .post(body)

        val response = makeRequest(request)
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            throw IOException(parseErrorMessage(response.code, responseBody, "Sinkronisasi"))
        }
        json.decodeFromString<SyncResponse>(responseBody)
    }

    private fun parseErrorMessage(code: Int, responseBody: String, fallbackAction: String): String {
        if (responseBody.isBlank()) {
            return when (code) {
                401 -> "Email atau kata sandi tidak sesuai."
                403 -> "Akses ditolak."
                404 -> "Layanan tidak ditemukan (404)."
                422 -> "Data yang dimasukkan tidak valid."
                429 -> "Terlalu banyak permintaan. Mohon tunggu beberapa saat."
                in 500..599 -> "Terjadi kendala pada server ($code). Silakan coba lagi nanti."
                else -> "$fallbackAction gagal ($code)"
            }
        }

        try {
            val jsonElement = json.parseToJsonElement(responseBody)
            if (jsonElement is JsonObject) {
                // 1. Check if there are field validation errors in 'errors' object
                val errorsObj = jsonElement["errors"]
                if (errorsObj is JsonObject && errorsObj.isNotEmpty()) {
                    val messages = mutableListOf<String>()
                    for ((_, value) in errorsObj) {
                        if (value is JsonArray) {
                            for (msg in value) {
                                val text = msg.jsonPrimitive.contentOrNull
                                if (!text.isNullOrBlank()) {
                                    messages.add(translateErrorMessage(text))
                                }
                            }
                        } else if (value is JsonPrimitive) {
                            val text = value.contentOrNull
                            if (!text.isNullOrBlank()) {
                                messages.add(translateErrorMessage(text))
                            }
                        }
                    }
                    if (messages.isNotEmpty()) {
                        return messages.first()
                    }
                }

                // 2. Check top-level 'message'
                val message = jsonElement["message"]?.jsonPrimitive?.contentOrNull
                if (!message.isNullOrBlank()) {
                    return translateErrorMessage(message)
                }

                // 3. Check top-level 'error'
                val error = jsonElement["error"]?.jsonPrimitive?.contentOrNull
                if (!error.isNullOrBlank()) {
                    return translateErrorMessage(error)
                }
            }
        } catch (_: Exception) {
            // Non-JSON response (e.g. HTML gateway 502/504)
        }

        return when (code) {
            401 -> "Email atau kata sandi tidak sesuai."
            403 -> "Akses ditolak atau dibatasi."
            404 -> "Data tidak ditemukan."
            422 -> "Data yang dimasukkan tidak valid."
            429 -> "Terlalu banyak percobaan. Silakan coba lagi beberapa saat lagi."
            in 500..599 -> "Terjadi gangguan pada server ($code). Coba beberapa saat lagi."
            else -> "$fallbackAction gagal ($code)"
        }
    }

    private fun translateErrorMessage(raw: String): String {
        val trimmed = raw.trim()
        val lower = trimmed.lowercase()

        return when {
            lower.contains("credentials do not match") || lower.contains("these credentials") ->
                "Email atau kata sandi salah. Silakan periksa kembali."

            lower.contains("email has already been taken") || lower.contains("email sudah terdaftar") ->
                "Alamat email sudah terdaftar. Silakan gunakan email lain atau masuk."

            lower.contains("password field must be at least") || lower.contains("password must be at least") ->
                "Kata sandi minimal harus 8 karakter."

            lower.contains("password confirmation does not match") || lower.contains("confirmation does not match") ->
                "Konfirmasi kata sandi tidak cocok."

            lower.contains("registration is restricted") ->
                "Pendaftaran akun dibatasi hanya untuk email yang telah diizinkan."

            lower.contains("registration is currently disabled") || lower.contains("public registration is disabled") ->
                "Pendaftaran akun baru saat ini sedang dinonaktifkan."

            lower.contains("too many requests") || lower.contains("too many attempts") ->
                "Terlalu banyak percobaan. Mohon tunggu 1 menit sebelum mencoba kembali."

            lower.contains("unauthenticated") ->
                "Sesi Anda telah berakhir. Silakan masuk kembali."

            lower.contains("the name field is required") ->
                "Nama lengkap wajib diisi."

            lower.contains("the email field is required") ->
                "Alamat email wajib diisi."

            lower.contains("the password field is required") ->
                "Kata sandi wajib diisi."

            lower.contains("valid email address") || lower.contains("must be a valid email") ->
                "Format alamat email tidak valid."

            else -> trimmed
        }
    }
}
