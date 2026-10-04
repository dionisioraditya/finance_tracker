package com.example.financetracker.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.financetracker.core.designsystem.CategoryIcons
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechButton
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.core.designsystem.components.MetricBadge
import com.example.financetracker.core.designsystem.components.MetricBadgeType
import com.example.financetracker.core.util.Formatters
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.local.entity.TransactionEntity
import com.example.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryTransactionsSheet(
    sheetState: SheetState,
    category: CategoryEntity,
    month: String,
    allocatedAmount: Double,
    transactions: List<TransactionEntity>,
    financeRepository: FinanceRepository,
    onDismiss: () -> Unit,
    onAddTransaction: (CategoryEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }

    val catColor = CategoryIcons.getColor(category.color)
    val catSubtleColor = CategoryIcons.getSubtleColor(category.color)
    val catIcon = CategoryIcons.getIcon(category.icon)

    val totalSpent = transactions.sumOf { it.amount }
    val remaining = allocatedAmount - totalSpent
    val progress = if (allocatedAmount > 0) (totalSpent / allocatedAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentString = if (allocatedAmount > 0) {
        String.format("%.1f%%", (totalSpent / allocatedAmount) * 100)
    } else {
        "0%"
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FintechColors.Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        ) {
            // Header: Category Icon, Name, and Close Action
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
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(catSubtleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = catIcon,
                            contentDescription = null,
                            tint = catColor,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = category.name,
                            style = FintechTypography.titleLarge,
                            color = FintechColors.TextPrimary
                        )
                        Text(
                            text = "${Formatters.formatMonthName(month)} • ${if (category.type == "expense") "Anggaran Bulanan" else "Cold Wallet"}",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = FintechColors.TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary Financial Card
            FintechCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 16.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Target Anggaran",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatCurrency(allocatedAmount),
                            style = FintechTypography.titleMedium,
                            color = FintechColors.TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Terpakai",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatCurrency(totalSpent),
                            style = FintechTypography.titleMedium,
                            color = if (totalSpent > allocatedAmount && allocatedAmount > 0) FintechColors.OutflowRed else FintechColors.TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Sisa Anggaran",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = Formatters.formatCurrency(remaining),
                            style = FintechTypography.titleMedium,
                            color = if (remaining >= 0) FintechColors.InflowGreen else FintechColors.OutflowRed
                        )
                    }
                }

                if (allocatedAmount > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (remaining < 0) FintechColors.OutflowRed else catColor,
                        trackColor = FintechColors.SurfaceSubtle
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$percentString dari target",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary
                        )
                        MetricBadge(
                            text = if (remaining >= 0) "Terkendali" else "Over Budget",
                            type = if (remaining >= 0) MetricBadgeType.INFLOW else MetricBadgeType.WARNING
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Section Header & Quick Add Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Riwayat Transaksi",
                        style = FintechTypography.titleMedium,
                        color = FintechColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    MetricBadge(
                        text = "${transactions.size}",
                        type = MetricBadgeType.NEUTRAL
                    )
                }

                TextButton(
                    onClick = {
                        onAddTransaction(category)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = FintechColors.Primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Catat",
                        style = FintechTypography.labelLarge,
                        color = FintechColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Transactions List or Empty State
            if (transactions.isEmpty()) {
                FintechCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(FintechColors.SurfaceSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ReceiptLong,
                                contentDescription = null,
                                tint = FintechColors.TextMuted,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Transaksi",
                            style = FintechTypography.titleMedium,
                            color = FintechColors.TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Belum ada pengeluaran untuk kategori ${category.name} pada bulan ini.",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        FintechButton(
                            text = "+ Catat Transaksi Baru",
                            onClick = {
                                onAddTransaction(category)
                            },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(transactions, key = { it.id }) { tx ->
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
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(FintechColors.OutflowRed.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowDownward,
                                            contentDescription = null,
                                            tint = FintechColors.OutflowRed,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = tx.name,
                                            style = FintechTypography.titleMedium,
                                            color = FintechColors.TextPrimary
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Rounded.CalendarToday,
                                                contentDescription = null,
                                                tint = FintechColors.TextMuted,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = Formatters.formatDate(tx.transactionDate),
                                                style = FintechTypography.bodySmall,
                                                color = FintechColors.TextSecondary
                                            )
                                            if (!tx.notes.isNullOrBlank()) {
                                                Text(
                                                    text = " • ${tx.notes}",
                                                    style = FintechTypography.bodySmall,
                                                    color = FintechColors.TextMuted,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = "-${Formatters.formatCurrency(tx.amount)}",
                                        style = FintechTypography.titleMedium,
                                        color = FintechColors.OutflowRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = { transactionToDelete = tx },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = "Hapus",
                                            tint = FintechColors.TextMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (transactionToDelete != null) {
        val target = transactionToDelete!!
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
            title = {
                Text(
                    text = "Hapus Transaksi?",
                    style = FintechTypography.titleLarge,
                    color = FintechColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus transaksi '${target.name}' sebesar ${Formatters.formatCurrency(target.amount)}?",
                    style = FintechTypography.bodyMedium,
                    color = FintechColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            financeRepository.deleteTransaction(target.id)
                            financeRepository.recomputeRolloverForMonth(category.userId, month)
                            transactionToDelete = null
                        }
                    }
                ) {
                    Text(
                        text = "Hapus",
                        style = FintechTypography.labelLarge,
                        color = FintechColors.OutflowRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text(
                        text = "Batal",
                        style = FintechTypography.labelLarge,
                        color = FintechColors.TextSecondary
                    )
                }
            },
            containerColor = FintechColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
