package com.example.financetracker.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.ExitToApp
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.datastore.UserPreferences
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechButton
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.core.designsystem.components.MetricBadge
import com.example.financetracker.core.designsystem.components.MetricBadgeType
import com.example.financetracker.data.repository.AuthRepository
import com.example.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    authRepository: AuthRepository,
    financeRepository: FinanceRepository,
    userPreferences: UserPreferences,
    onManageCategories: () -> Unit = {},
    onLogout: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val userName by authRepository.userName.collectAsState(initial = "User")
    val userEmail by authRepository.userEmail.collectAsState(initial = "")
    val lastSync by userPreferences.lastSyncTimestamp.collectAsState(initial = null)
    var isSyncing by remember { mutableStateOf(false) }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Pengaturan & Akun",
            style = FintechTypography.headlineMedium,
            color = FintechColors.TextInverse
        )

        // Profile Card
        FintechCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 18.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(FintechColors.PrimaryLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        tint = FintechColors.Primary,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = userName ?: "Pengguna Keuangan",
                        style = FintechTypography.titleLarge,
                        color = FintechColors.TextPrimary
                    )
                    Text(
                        text = userEmail.orEmpty(),
                        style = FintechTypography.bodyMedium,
                        color = FintechColors.TextSecondary
                    )
                }
            }
        }

        // Manage Categories Card
        FintechCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onManageCategories() },
            contentPadding = 18.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(FintechColors.PrimaryLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Category,
                            contentDescription = null,
                            tint = FintechColors.Primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Kelola Kategori",
                            style = FintechTypography.titleMedium,
                            color = FintechColors.TextPrimary
                        )
                        Text(
                            text = "Tambah, ubah, atau nonaktifkan kategori",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary
                        )
                    }
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    contentDescription = null,
                    tint = FintechColors.TextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Synchronization Card
        FintechCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 18.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = FintechColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Status Sinkronisasi",
                        style = FintechTypography.titleMedium,
                        color = FintechColors.TextPrimary
                    )
                }

                MetricBadge(
                    text = if (lastSync != null) "Tersinkron" else "Belum Sinkron",
                    type = if (lastSync != null) MetricBadgeType.INFLOW else MetricBadgeType.WARNING
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Terakhir: ${lastSync ?: "Belum pernah sinkron"}",
                style = FintechTypography.bodySmall,
                color = FintechColors.TextSecondary
            )

            if (syncMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = syncMessage.orEmpty(),
                    style = FintechTypography.bodySmall,
                    color = FintechColors.Primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FintechButton(
                text = "Sinkronkan Sekarang",
                isLoading = isSyncing,
                onClick = {
                    coroutineScope.launch {
                        isSyncing = true
                        val result = financeRepository.performSync()
                        isSyncing = false
                        syncMessage = if (result.isSuccess) {
                            "Sinkronisasi berhasil!"
                        } else {
                            "Gagal: ${result.exceptionOrNull()?.message}"
                        }
                    }
                }
            )
        }

        // Logout Button
        FintechButton(
            text = "Keluar dari Akun",
            leadingIcon = Icons.Rounded.ExitToApp,
            backgroundColor = FintechColors.OutflowRedBg,
            contentColor = FintechColors.OutflowRed,
            onClick = {
                coroutineScope.launch {
                    authRepository.logout()
                    onLogout()
                }
            }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }
}
