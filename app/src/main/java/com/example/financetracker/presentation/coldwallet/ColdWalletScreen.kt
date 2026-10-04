package com.example.financetracker.presentation.coldwallet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.core.designsystem.components.MetricBadge
import com.example.financetracker.core.designsystem.components.MetricBadgeType
import com.example.financetracker.core.util.Formatters
import com.example.financetracker.data.repository.FinanceRepository
import com.example.financetracker.presentation.transaction.AddTransactionSheet
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColdWalletScreen(
    userId: Long,
    financeRepository: FinanceRepository
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Sisa Bulanan, 1: Mutasi Eksternal
    var isAddSheetVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(userId) {
        financeRepository.syncRolloversWithBudgets(userId)
        financeRepository.performSync()
    }

    val rollovers by financeRepository.getRollovers(userId).collectAsState(initial = emptyList())
    val mutations by financeRepository.getColdWalletMutations(userId).collectAsState(initial = emptyList())
    val categories by financeRepository.getCategories(userId).collectAsState(initial = emptyList())

    // Calculations
    val accumulatedMonthly = rollovers.filter { it.isTransferred }.sumOf { it.remainingAmount }
    val externalInflow = mutations.filter { it.transactionType == "external_inflow" }.sumOf { it.amount }
    val externalOutflow = mutations.filter { it.transactionType == "external_outflow" }.sumOf { it.amount }
    val netExternalProfit = externalInflow - externalOutflow
    val totalColdWalletBalance = accumulatedMonthly + netExternalProfit

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Cold Wallet & Aset",
                    style = FintechTypography.headlineMedium,
                    color = FintechColors.TextInverse
                )
                Text(
                    text = "Pusat akumulasi sisa anggaran & mutasi profit eksternal",
                    style = FintechTypography.bodyMedium,
                    color = FintechColors.TextInverse
                )
            }

            // Hero Card: Total Saldo Cold Wallet
            item {
                FintechCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = 20.dp
                ) {
                    Text(
                        text = "Total Saldo Cold Wallet",
                        style = FintechTypography.bodyMedium,
                        color = FintechColors.TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = Formatters.formatCurrency(totalColdWalletBalance),
                        style = FintechTypography.displayLarge,
                        color = FintechColors.Primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FintechColors.SurfaceSubtle)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Sisa Bulanan Terkumpul",
                                    style = FintechTypography.bodySmall,
                                    color = FintechColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatCurrency(accumulatedMonthly),
                                    style = FintechTypography.titleMedium,
                                    color = FintechColors.TextPrimary
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(FintechColors.SurfaceSubtle)
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Net Profit Eksternal",
                                    style = FintechTypography.bodySmall,
                                    color = FintechColors.TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatCurrency(netExternalProfit),
                                    style = FintechTypography.titleMedium,
                                    color = if (netExternalProfit >= 0) FintechColors.InflowGreen else FintechColors.OutflowRed
                                )
                            }
                        }
                    }
                }
            }

            // Tab Selector: Sisa Anggaran Bulanan vs Mutasi Eksternal
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FintechColors.SurfaceSubtle)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 0) FintechColors.Surface else FintechColors.SurfaceSubtle)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sisa Budget Bulanan",
                            style = FintechTypography.labelMedium,
                            color = if (selectedTab == 0) FintechColors.Primary else FintechColors.TextSecondary,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (selectedTab == 1) FintechColors.Surface else FintechColors.SurfaceSubtle)
                            .clickable { selectedTab = 1 }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Buku Mutasi Eksternal",
                            style = FintechTypography.labelMedium,
                            color = if (selectedTab == 1) FintechColors.Primary else FintechColors.TextSecondary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Tab Content
            if (selectedTab == 0) {
                // Sisa Budget Bulanan List with Checkboxes
                if (rollovers.isEmpty()) {
                    item {
                        FintechCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Belum ada rekaman sisa anggaran bulanan.",
                                style = FintechTypography.bodyMedium,
                                color = FintechColors.TextSecondary
                            )
                        }
                    }
                } else {
                    items(rollovers) { rollover ->
                        FintechCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 14.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = Formatters.formatMonthName(rollover.month),
                                        style = FintechTypography.titleMedium,
                                        color = FintechColors.TextPrimary
                                    )
                                    Text(
                                        text = "Sisa: ${Formatters.formatCurrency(rollover.remainingAmount)}",
                                        style = FintechTypography.bodySmall,
                                        color = FintechColors.TextSecondary
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (rollover.isTransferred) "Terkumpul" else "Belum Ditransfer",
                                        style = FintechTypography.labelSmall,
                                        color = if (rollover.isTransferred) FintechColors.InflowGreen else FintechColors.TextMuted
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Checkbox(
                                        checked = rollover.isTransferred,
                                        onCheckedChange = {
                                            coroutineScope.launch {
                                                financeRepository.toggleRolloverTransfer(rollover)
                                                launch { financeRepository.performSync() }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = FintechColors.Primary)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Buku Mutasi Eksternal (Inflows & Outflows)
                if (mutations.isEmpty()) {
                    item {
                        FintechCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Belum ada mutasi eksternal. Tekan (+) untuk mencatat profit investasi atau alokasi aset.",
                                style = FintechTypography.bodyMedium,
                                color = FintechColors.TextSecondary
                            )
                        }
                    }
                } else {
                    items(mutations) { mut ->
                        val isInflow = mut.transactionType == "external_inflow"

                        FintechCard(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = 12.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isInflow) FintechColors.InflowGreenBg else FintechColors.OutflowRedBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isInflow) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (isInflow) FintechColors.InflowGreen else FintechColors.OutflowRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = mut.name,
                                            style = FintechTypography.titleMedium,
                                            color = FintechColors.TextPrimary
                                        )
                                        Text(
                                            text = Formatters.formatDate(mut.transactionDate),
                                            style = FintechTypography.bodySmall,
                                            color = FintechColors.TextSecondary
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = (if (isInflow) "+ " else "- ") + Formatters.formatCurrency(mut.amount),
                                        style = FintechTypography.titleMedium,
                                        color = if (isInflow) FintechColors.InflowGreen else FintechColors.OutflowRed
                                    )
                                    MetricBadge(
                                        text = if (isInflow) "Masuk" else "Keluar",
                                        type = if (isInflow) MetricBadgeType.INFLOW else MetricBadgeType.OUTFLOW
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Transaction Sheet for Cold Wallet
        if (isAddSheetVisible) {
            AddTransactionSheet(
                sheetState = sheetState,
                userId = userId,
                categories = categories,
                financeRepository = financeRepository,
                onDismiss = { isAddSheetVisible = false }
            )
        }
    }
}
