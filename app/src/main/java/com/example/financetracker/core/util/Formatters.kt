package com.example.financetracker.core.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Formatters {
    private val rupiahLocale = Locale.forLanguageTag("id-ID")
    private val numberFormat = NumberFormat.getNumberInstance(rupiahLocale)

    /**
     * Formats a number to IDR currency representation.
     * Example: 1522489 -> "IDR 1.522.489"
     */
    fun formatCurrency(amount: Double, prefix: String = "IDR"): String {
        return "$prefix ${numberFormat.format(amount.toLong())}"
    }

    /**
     * Formats date string "YYYY-MM-DD" to human readable "10 Sep 2026".
     */
    fun formatDate(dateString: String): String {
        return try {
            val date = LocalDate.parse(dateString)
            val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", rupiahLocale)
            date.format(formatter)
        } catch (e: Exception) {
            dateString
        }
    }

    /**
     * Converts current date to "YYYY-MM-DD".
     */
    fun todayDateString(): String {
        return LocalDate.now().toString()
    }

    /**
     * Converts current month to "YYYY-MM".
     */
    fun currentMonthString(): String {
        val now = LocalDate.now()
        val month = now.monthValue.toString().padStart(2, '0')
        return "${now.year}-$month"
    }

    /**
     * Format month string "YYYY-MM" to "September 2026".
     */
    fun formatMonthName(monthString: String): String {
        return try {
            val parts = monthString.split("-")
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val date = LocalDate.of(year, month, 1)
            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", rupiahLocale)
            date.format(formatter)
        } catch (e: Exception) {
            monthString
        }
    }

    /**
     * Converts raw exception messages (network errors, timeouts, etc.) into clean user-friendly text.
     */
    fun humanizeError(throwable: Throwable?): String {
        if (throwable == null) return "Terjadi kesalahan yang tidak diketahui."
        val msg = throwable.message?.trim().orEmpty()

        return when {
            throwable is java.net.UnknownHostException ->
                "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."

            throwable is java.net.ConnectException ->
                "Gagal terhubung ke server. Pastikan server aktif dan internet stabil."

            throwable is java.net.SocketTimeoutException ->
                "Waktu koneksi habis (timeout). Silakan coba beberapa saat lagi."

            msg.contains("Failed to connect") || msg.contains("Connection refused") ->
                "Koneksi ke server gagal. Periksa jaringan internet Anda."

            msg.contains("timeout", ignoreCase = true) ->
                "Waktu permintaan habis. Silakan coba lagi."

            msg.isNotBlank() -> msg
            else -> "Terjadi kesalahan pada sistem. Silakan coba lagi."
        }
    }
}
