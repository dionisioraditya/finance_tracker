package com.example.financetracker.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.datastore.UserPreferences
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.FinanceRepository
import com.example.financetracker.presentation.auth.AuthScreen
import com.example.financetracker.presentation.category.ManageCategoriesScreen
import com.example.financetracker.presentation.coldwallet.ColdWalletScreen
import com.example.financetracker.presentation.dashboard.DashboardScreen
import com.example.financetracker.presentation.settings.SettingsScreen
import com.example.financetracker.presentation.transaction.TransactionHistoryScreen
import kotlinx.coroutines.launch

@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun MainNavigation(
    authRepository: AuthRepository,
    financeRepository: FinanceRepository,
    userPreferences: UserPreferences
) {
    val isLoggedIn by authRepository.isLoggedIn.collectAsState(initial = false)
    val userId by authRepository.userId.collectAsState(initial = null)

    if (!isLoggedIn) {
        AuthScreen(
            authRepository = authRepository,
            financeRepository = financeRepository,
            userPreferences = userPreferences,
            onAuthSuccess = { }
        )
    } else {
        val activeUserId = userId ?: 1L
        val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            initialPage = 0,
            pageCount = { Screen.bottomNavItems.size }
        )
        var isManageCategoriesOpen by remember { mutableStateOf(false) }
        var isGlobalAddSheetVisible by remember { mutableStateOf(false) }
        val globalAddSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val categories by financeRepository.getCategories(activeUserId).collectAsState(initial = emptyList())

        val currentScreen = if (isManageCategoriesOpen) {
            Screen.ManageCategories
        } else {
            Screen.bottomNavItems.getOrElse(pagerState.currentPage) { Screen.Dashboard }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FintechColors.topBackgroundBrush())
        ) {
            // Horizontal Pager for Smooth Slide Transitions & Gestures
            androidx.compose.foundation.pager.HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { page ->
                when (Screen.bottomNavItems[page]) {
                    Screen.Dashboard -> DashboardScreen(
                        userId = activeUserId,
                        financeRepository = financeRepository
                    )
                    Screen.ColdWallet -> ColdWalletScreen(
                        userId = activeUserId,
                        financeRepository = financeRepository
                    )
                    Screen.Transactions -> TransactionHistoryScreen(
                        userId = activeUserId,
                        financeRepository = financeRepository
                    )
                    Screen.Settings -> SettingsScreen(
                        authRepository = authRepository,
                        financeRepository = financeRepository,
                        userPreferences = userPreferences,
                        onManageCategories = { isManageCategoriesOpen = true },
                        onLogout = { }
                    )
                    else -> Unit
                }
            }

            // Subscreen Slide Overlay: Manage Categories
            androidx.compose.animation.AnimatedVisibility(
                visible = isManageCategoriesOpen,
                enter = androidx.compose.animation.slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                ) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 300)
                ) + androidx.compose.animation.fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                ManageCategoriesScreen(
                    userId = activeUserId,
                    financeRepository = financeRepository,
                    onBack = { isManageCategoriesOpen = false }
                )
            }

            // Floating Navigation Bar with Center "+" Action Button
            if (!isManageCategoriesOpen) {
                FloatingBottomBar(
                    pagerState = pagerState,
                    isSubscreenOpen = isManageCategoriesOpen,
                    onScreenSelected = { screen ->
                        val targetIndex = Screen.bottomNavItems.indexOf(screen)
                        if (targetIndex >= 0) {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    page = targetIndex,
                                    animationSpec = androidx.compose.animation.core.tween(durationMillis = 350)
                                )
                            }
                        }
                    },
                    onAddClick = { isGlobalAddSheetVisible = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }

            // Global Add Transaction Sheet
            if (isGlobalAddSheetVisible) {
                com.example.financetracker.presentation.transaction.AddTransactionSheet(
                    sheetState = globalAddSheetState,
                    userId = activeUserId,
                    categories = categories,
                    financeRepository = financeRepository,
                    onDismiss = { isGlobalAddSheetVisible = false }
                )
            }
        }
    }
}
