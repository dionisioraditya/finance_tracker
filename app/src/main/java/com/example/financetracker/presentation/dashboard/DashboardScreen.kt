package com.example.financetracker.presentation.dashboard

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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import com.example.financetracker.core.designsystem.CategoryIcons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
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
fun DashboardScreen(
    userId: Long,
    financeRepository: FinanceRepository
) {
    var selectedMonth by remember { mutableStateOf(Formatters.currentMonthString()) }
    var isAddSheetVisible by remember { mutableStateOf(false) }
    var isBudgetSheetVisible by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val budgetSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categoryDetailSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedCategoryForDetail by remember { mutableStateOf<com.example.financetracker.data.local.entity.CategoryEntity?>(null) }
    var preselectedCategoryForAdd by remember { mutableStateOf<com.example.financetracker.data.local.entity.CategoryEntity?>(null) }

    val categories by financeRepository.getCategories(userId).collectAsState(initial = emptyList())
    val monthlyBudget by financeRepository.getMonthlyBudget(userId, selectedMonth).collectAsState(initial = null)
    val categoryBudgets by financeRepository.getCategoryBudgets(monthlyBudget?.id.orEmpty()).collectAsState(initial = emptyList())
    val transactions by financeRepository.getMonthlyExpenses(userId, selectedMonth).collectAsState(initial = emptyList())

    // Calculations
    val totalBudget = monthlyBudget?.totalBudget ?: 0.0
    val totalSpending = transactions.sumOf { it.amount }
    val remaining = totalBudget - totalSpending
    val spentPercentage = if (totalBudget > 0) (totalSpending / totalBudget).toFloat().coerceIn(0f, 1f) else 0f

    val categoryMap = categories.associateBy { it.id }

    Scaffold(
        containerColor = Color.Transparent
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
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.financetracker.R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )
                        Column {
                            Text(
                                text = "Laporan Keuangan",
                                style = FintechTypography.headlineMedium,
                                color = FintechColors.TextInverse
                            )
                            Text(
                                text = Formatters.formatMonthName(selectedMonth),
                                style = FintechTypography.bodyMedium,
                                color = FintechColors.TextInverse
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    isSyncing = true
                                    financeRepository.performSync()
                                    isSyncing = false
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSyncing) Icons.Rounded.Sync else Icons.Rounded.CheckCircle,
                                contentDescription = "Sync",
                                tint = if (isSyncing) FintechColors.Primary else FintechColors.InflowGreen
                            )
                        }
                    }
                }
            }

            // Hero Card: Total Budget & Spending
            item {
                FintechCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isBudgetSheetVisible = true },
                    backgroundColor = FintechColors.Surface,
                    contentPadding = 20.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Target Anggaran Bulanan",
                                style = FintechTypography.bodyMedium,
                                color = FintechColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Atur Anggaran",
                                tint = FintechColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        MetricBadge(
                            text = if (remaining >= 0) "Terkendali" else "Over Budget",
                            type = if (remaining >= 0) MetricBadgeType.INFLOW else MetricBadgeType.OUTFLOW
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = Formatters.formatCurrency(totalBudget),
                        style = FintechTypography.displayLarge,
                        color = FintechColors.TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar
                    LinearProgressIndicator(
                        progress = { spentPercentage },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (spentPercentage > 0.9f) FintechColors.OutflowRed else FintechColors.Primary,
                        trackColor = FintechColors.SurfaceSubtle,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Terpakai",
                                style = FintechTypography.bodySmall,
                                color = FintechColors.TextSecondary
                            )
                            Text(
                                text = Formatters.formatCurrency(totalSpending),
                                style = FintechTypography.titleMedium,
                                color = FintechColors.TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Sisa Anggaran",
                                style = FintechTypography.bodySmall,
                                color = FintechColors.TextSecondary
                            )
                            Text(
                                text = Formatters.formatCurrency(remaining),
                                style = FintechTypography.titleMedium,
                                color = if (remaining >= 0) FintechColors.InflowGreen else FintechColors.OutflowRed
                            )
                        }
                    }
                }
            }

            // Category Breakdown Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alokasi per Kategori",
                        style = FintechTypography.titleLarge,
                        color = FintechColors.TextPrimary
                    )
                    TextButton(onClick = { isBudgetSheetVisible = true }) {
                        Text(
                            text = "Atur Target",
                            style = FintechTypography.labelLarge,
                            color = FintechColors.Primary
                        )
                    }
                }
            }

            // Category Cards
            val expenseCategories = categories.filter { it.type == "expense" }
            if (expenseCategories.isEmpty()) {
                item {
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Belum ada kategori pengeluaran. Kategori bawaan akan otomatis dibuat saat Anda melakukan sinkronisasi.",
                            style = FintechTypography.bodyMedium,
                            color = FintechColors.TextSecondary
                        )
                    }
                }
            } else {
                items(expenseCategories) { cat ->
                    val allocated = categoryBudgets.firstOrNull { it.categoryId == cat.id }?.allocatedAmount ?: 0.0
                    val spent = transactions.filter { it.categoryId == cat.id }.sumOf { it.amount }
                    val catRemaining = allocated - spent
                    val progress = if (allocated > 0) (spent / allocated).toFloat().coerceIn(0f, 1f) else 0f

                    FintechCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedCategoryForDetail = cat
                            },
                        contentPadding = 14.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val catColor = CategoryIcons.getColor(cat.color)
                                val catSubtleColor = CategoryIcons.getSubtleColor(cat.color)
                                val catIcon = CategoryIcons.getIcon(cat.icon)

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(catSubtleColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = catIcon,
                                        contentDescription = null,
                                        tint = catColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = cat.name,
                                        style = FintechTypography.titleMedium,
                                        color = FintechColors.TextPrimary
                                    )
                                    Text(
                                        text = "Budget: ${Formatters.formatCurrency(allocated)}",
                                        style = FintechTypography.bodySmall,
                                        color = FintechColors.TextSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = Formatters.formatCurrency(spent),
                                    style = FintechTypography.titleMedium,
                                    color = FintechColors.TextPrimary
                                )
                                Text(
                                    text = "Sisa: ${Formatters.formatCurrency(catRemaining)}",
                                    style = FintechTypography.bodySmall,
                                    color = if (catRemaining >= 0) FintechColors.TextSecondary else FintechColors.OutflowRed
                                )
                            }
                        }

                        if (allocated > 0) {
                            Spacer(modifier = Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (catRemaining < 0) FintechColors.OutflowRed else FintechColors.Primary,
                                trackColor = FintechColors.SurfaceSubtle
                            )
                        }
                    }
                }
            }

            // Recent Transactions Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Transaksi Terakhir",
                    style = FintechTypography.titleLarge,
                    color = FintechColors.TextPrimary
                )
            }

            if (transactions.isEmpty()) {
                item {
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Belum ada transaksi bulan ini. Tekan tombol (+) untuk mencatat pengeluaran pertama Anda.",
                            style = FintechTypography.bodyMedium,
                            color = FintechColors.TextSecondary
                        )
                    }
                }
            } else {
                items(transactions.take(5)) { tx ->
                    val categoryName = categoryMap[tx.categoryId]?.name ?: "Lainnya"

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
                                        .background(FintechColors.OutflowRedBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ArrowDownward,
                                        contentDescription = null,
                                        tint = FintechColors.OutflowRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = tx.name,
                                        style = FintechTypography.titleMedium,
                                        color = FintechColors.TextPrimary
                                    )
                                    Text(
                                        text = "$categoryName • ${Formatters.formatDate(tx.transactionDate)}",
                                        style = FintechTypography.bodySmall,
                                        color = FintechColors.TextSecondary
                                    )
                                }
                            }

                            Text(
                                text = "- ${Formatters.formatCurrency(tx.amount)}",
                                style = FintechTypography.titleMedium,
                                color = FintechColors.OutflowRed
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Add Transaction Modal Bottom Sheet
        if (isAddSheetVisible) {
            AddTransactionSheet(
                sheetState = sheetState,
                userId = userId,
                categories = categories,
                financeRepository = financeRepository,
                initialCategoryId = preselectedCategoryForAdd?.id,
                onDismiss = {
                    isAddSheetVisible = false
                    preselectedCategoryForAdd = null
                }
            )
        }

        // Set Monthly Budget Modal Bottom Sheet
        if (isBudgetSheetVisible) {
            SetBudgetSheet(
                sheetState = budgetSheetState,
                userId = userId,
                month = selectedMonth,
                currentTotalBudget = totalBudget,
                categories = categories,
                currentAllocations = categoryBudgets,
                financeRepository = financeRepository,
                onDismiss = { isBudgetSheetVisible = false }
            )
        }

        // Category Detail & Transactions Bottom Sheet
        if (selectedCategoryForDetail != null) {
            val cat = selectedCategoryForDetail!!
            val allocated = categoryBudgets.firstOrNull { it.categoryId == cat.id }?.allocatedAmount ?: 0.0
            val catTransactions = transactions.filter { it.categoryId == cat.id }
            CategoryTransactionsSheet(
                sheetState = categoryDetailSheetState,
                category = cat,
                month = selectedMonth,
                allocatedAmount = allocated,
                transactions = catTransactions,
                financeRepository = financeRepository,
                onDismiss = { selectedCategoryForDetail = null },
                onAddTransaction = { targetCat ->
                    selectedCategoryForDetail = null
                    preselectedCategoryForAdd = targetCat
                    isAddSheetVisible = true
                }
            )
        }
    }
}
