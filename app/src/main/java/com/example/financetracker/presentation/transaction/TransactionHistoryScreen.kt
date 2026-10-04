package com.example.financetracker.presentation.transaction

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
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.core.designsystem.components.FintechTextField
import com.example.financetracker.core.designsystem.components.MetricBadge
import com.example.financetracker.core.designsystem.components.MetricBadgeType
import com.example.financetracker.core.util.Formatters
import com.example.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.launch

@Composable
fun TransactionHistoryScreen(
    userId: Long,
    financeRepository: FinanceRepository
) {
    var searchQuery by remember { mutableStateOf("") }
    var filterType by remember { mutableStateOf("ALL") } // ALL, MONTHLY, COLD_WALLET

    val coroutineScope = rememberCoroutineScope()
    val allTransactions by financeRepository.getAllTransactions(userId).collectAsState(initial = emptyList())
    val categories by financeRepository.getCategories(userId).collectAsState(initial = emptyList())
    val categoryMap = categories.associateBy { it.id }

    val filtered = allTransactions.filter { tx ->
        val matchesSearch = tx.name.contains(searchQuery, ignoreCase = true) ||
                (tx.notes?.contains(searchQuery, ignoreCase = true) == true)
        val matchesFilter = when (filterType) {
            "MONTHLY" -> tx.walletType == "monthly_budget"
            "COLD_WALLET" -> tx.walletType == "cold_wallet"
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Riwayat Transaksi",
            style = FintechTypography.headlineMedium,
            color = FintechColors.TextInverse
        )
        Text(
            text = "Daftar seluruh mutasi finansial tersimpan",
            style = FintechTypography.bodyMedium,
            color = FintechColors.TextInverse
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Search Input
        FintechTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = "Cari Transaksi",
            placeholder = "Ketik nama atau catatan...",
            leadingIcon = Icons.Rounded.Search
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filter Pills
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val filters = listOf(
                "ALL" to "Semua",
                "MONTHLY" to "Anggaran Bulanan",
                "COLD_WALLET" to "Cold Wallet"
            )

            filters.forEach { (key, label) ->
                val isSelected = filterType == key
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) FintechColors.Primary else FintechColors.SurfaceSubtle)
                        .clickable { filterType = key }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = label,
                        style = FintechTypography.labelSmall,
                        color = if (isSelected) FintechColors.TextInverse else FintechColors.TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Tidak ada transaksi yang cocok",
                    style = FintechTypography.bodyMedium,
                    color = FintechColors.TextSecondary
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered, key = { it.id }) { tx ->
                    val isInflow = tx.transactionType == "external_inflow"
                    val categoryName = categoryMap[tx.categoryId]?.name ?: if (tx.walletType == "cold_wallet") "Cold Wallet" else "Lainnya"

                    FintechCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = 12.dp
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
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

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = (if (isInflow) "+ " else "- ") + Formatters.formatCurrency(tx.amount),
                                        style = FintechTypography.titleMedium,
                                        color = if (isInflow) FintechColors.InflowGreen else FintechColors.OutflowRed
                                    )
                                    MetricBadge(
                                        text = if (tx.isSynced) "Synced" else "Pending",
                                        type = if (tx.isSynced) MetricBadgeType.NEUTRAL else MetricBadgeType.WARNING
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            financeRepository.deleteTransaction(tx.id)
                                            launch { financeRepository.performSync() }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Hapus",
                                        tint = FintechColors.TextMuted,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
