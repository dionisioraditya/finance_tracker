package com.example.financetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.financetracker.core.database.FinanceDatabase
import com.example.financetracker.core.datastore.UserPreferences
import com.example.financetracker.core.designsystem.FinanceTrackerTheme
import com.example.financetracker.data.remote.api.ApiClient
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.FinanceRepository
import com.example.financetracker.data.sync.FinanceSyncWorker
import com.example.financetracker.presentation.navigation.MainNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPreferences = UserPreferences(applicationContext)
        val database = FinanceDatabase.getDatabase(applicationContext)
        val apiClient = ApiClient(userPreferences)
        val financeRepository = FinanceRepository(database, apiClient, userPreferences)
        val authRepository = AuthRepository(apiClient, userPreferences, database)

        // Enqueue periodic background sync with server
        FinanceSyncWorker.enqueuePeriodicSync(applicationContext)

        setContent {
            FinanceTrackerTheme {
                MainNavigation(
                    authRepository = authRepository,
                    financeRepository = financeRepository,
                    userPreferences = userPreferences
                )
            }
        }
    }
}