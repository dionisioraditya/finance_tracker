package com.example.financetracker.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechButton
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.core.designsystem.components.FintechTextField
import com.example.financetracker.core.designsystem.components.MetricBadge
import com.example.financetracker.core.designsystem.components.MetricBadgeType
import com.example.financetracker.core.util.Formatters
import com.example.financetracker.data.local.entity.CategoryBudgetEntity
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SetBudgetSheet(
    sheetState: SheetState,
    userId: Long,
    month: String,
    currentTotalBudget: Double,
    categories: List<CategoryEntity>,
    currentAllocations: List<CategoryBudgetEntity>,
    financeRepository: FinanceRepository,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    var totalBudgetText by remember {
        mutableStateOf(if (currentTotalBudget > 0) currentTotalBudget.toLong().toString() else "")
    }

    val allocationMap = remember {
        mutableStateMapOf<String, String>().apply {
            val existing = currentAllocations.associate { it.categoryId to it.allocatedAmount }
            categories.filter { it.type == "expense" }.forEach { cat ->
                val amount = existing[cat.id] ?: 0.0
                put(cat.id, if (amount > 0) amount.toLong().toString() else "")
            }
        }
    }

    val monthlyCategories = remember(categories) {
        categories.filter { it.type == "expense" }
    }

    val totalBudgetDouble = totalBudgetText.toDoubleOrNull() ?: 0.0
    val totalAllocated = allocationMap.values.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val difference = totalBudgetDouble - totalAllocated

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FintechColors.Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Atur Anggaran Bulanan",
                        style = FintechTypography.titleLarge,
                        color = FintechColors.TextPrimary
                    )
                    Text(
                        text = "Periode: ${Formatters.formatMonthName(month)}",
                        style = FintechTypography.bodyMedium,
                        color = FintechColors.TextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(FintechColors.SurfaceSubtle)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Tutup",
                        tint = FintechColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Total Budget Input
            FintechTextField(
                value = totalBudgetText,
                onValueChange = { totalBudgetText = it.filter { ch -> ch.isDigit() } },
                label = "Target Anggaran Bulanan (IDR)",
                placeholder = "Contoh: 2500000",
                leadingIcon = Icons.Rounded.AccountBalance,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Preset Buttons
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1_500_000L, 2_000_000L, 2_500_000L, 3_000_000L).forEach { preset ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechColors.PrimaryLight)
                            .clickable { totalBudgetText = preset.toString() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = Formatters.formatCurrency(preset.toDouble()),
                            style = FintechTypography.labelSmall,
                            color = FintechColors.Primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Allocations Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alokasi per Kategori",
                    style = FintechTypography.titleMedium,
                    color = FintechColors.TextPrimary
                )

                MetricBadge(
                    text = when {
                        totalBudgetDouble == 0.0 -> "Belum ada target"
                        difference == 0.0 -> "100% Pas"
                        difference > 0.0 -> "Sisa ${Formatters.formatCurrency(difference)}"
                        else -> "Lebih ${Formatters.formatCurrency(-difference)}"
                    },
                    type = when {
                        totalBudgetDouble == 0.0 -> MetricBadgeType.NEUTRAL
                        difference == 0.0 -> MetricBadgeType.INFLOW
                        difference > 0.0 -> MetricBadgeType.WARNING
                        else -> MetricBadgeType.OUTFLOW
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of category budget inputs
            monthlyCategories.forEach { category ->
                FintechCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    backgroundColor = FintechColors.SurfaceSubtle,
                    contentPadding = 12.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(FintechColors.PrimaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Category,
                                    contentDescription = null,
                                    tint = FintechColors.Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = category.name,
                                style = FintechTypography.titleMedium,
                                color = FintechColors.TextPrimary
                            )
                        }

                        // Input field for category allocation
                        Box(modifier = Modifier.width(150.dp)) {
                            FintechTextField(
                                value = allocationMap[category.id].orEmpty(),
                                onValueChange = { newVal ->
                                    allocationMap[category.id] = newVal.filter { it.isDigit() }
                                },
                                label = "IDR",
                                placeholder = "0",
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            FintechButton(
                text = "Simpan Anggaran",
                isLoading = isSaving,
                onClick = {
                    val finalTotal = totalBudgetText.toDoubleOrNull() ?: totalAllocated
                    val allocations = allocationMap.mapValues { it.value.toDoubleOrNull() ?: 0.0 }

                    isSaving = true
                    coroutineScope.launch {
                        financeRepository.saveMonthlyBudget(
                            userId = userId,
                            month = month,
                            totalBudget = finalTotal,
                            allocations = allocations
                        )
                        // Trigger background sync
                        launch { financeRepository.performSync() }
                        isSaving = false
                        onDismiss()
                    }
                }
            )

            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}
