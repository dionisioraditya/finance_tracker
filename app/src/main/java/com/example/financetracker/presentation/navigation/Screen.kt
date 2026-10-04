package com.example.financetracker.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector? = null) {
    data object Auth : Screen("auth", "Authentication")
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Rounded.Home)
    data object ColdWallet : Screen("cold_wallet", "Cold Wallet", Icons.Rounded.AccountBalanceWallet)
    data object Transactions : Screen("transactions", "Riwayat", Icons.Rounded.History)
    data object Settings : Screen("settings", "Pengaturan", Icons.Rounded.Settings)
    data object ManageCategories : Screen("manage_categories", "Kelola Kategori")

    companion object {
        val bottomNavItems: List<Screen>
            get() = listOf(Dashboard, ColdWallet, Transactions, Settings)
    }
}
