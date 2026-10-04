package com.example.financetracker.presentation.transaction

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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import android.app.DatePickerDialog
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechButton
import com.example.financetracker.core.designsystem.components.FintechTextField
import com.example.financetracker.core.util.Formatters
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.repository.FinanceRepository
import com.example.financetracker.presentation.category.CategoryFormSheet
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionSheet(
    sheetState: SheetState,
    userId: Long,
    categories: List<CategoryEntity>,
    financeRepository: FinanceRepository,
    initialCategoryId: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var amountText by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(initialCategoryId ?: categories.firstOrNull()?.id) }
    var walletType by remember { mutableStateOf("monthly_budget") } // monthly_budget or cold_wallet
    var coldWalletType by remember { mutableStateOf("external_inflow") } // external_inflow or external_outflow
    var notes by remember { mutableStateOf("") }
    var dateString by remember { mutableStateOf(Formatters.todayDateString()) }
    var isAddCategorySheetVisible by remember { mutableStateOf(false) }

    val todayString = remember { Formatters.todayDateString() }
    val yesterdayString = remember {
        try {
            LocalDate.now().minusDays(1).toString()
        } catch (e: Exception) {
            ""
        }
    }

    val openDatePicker = {
        val cal = Calendar.getInstance()
        val parts = dateString.split("-")
        val y = parts.getOrNull(0)?.toIntOrNull() ?: cal.get(Calendar.YEAR)
        val m = (parts.getOrNull(1)?.toIntOrNull() ?: (cal.get(Calendar.MONTH) + 1)) - 1
        val d = parts.getOrNull(2)?.toIntOrNull() ?: cal.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            com.example.financetracker.R.style.FintechDatePickerDialogTheme,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedMonth = (selectedMonth + 1).toString().padStart(2, '0')
                val formattedDay = selectedDay.toString().padStart(2, '0')
                dateString = "$selectedYear-$formattedMonth-$formattedDay"
            },
            y,
            m,
            d
        ).show()
    }

    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FintechColors.Surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Catat Transaksi",
                    style = FintechTypography.titleLarge,
                    color = FintechColors.TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = FintechColors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wallet Selector (Segmented Tab: Anggaran Bulanan vs Cold Wallet)
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
                        .background(if (walletType == "monthly_budget") FintechColors.Surface else FintechColors.SurfaceSubtle)
                        .clickable { walletType = "monthly_budget" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Anggaran Bulanan",
                        style = FintechTypography.labelMedium,
                        color = if (walletType == "monthly_budget") FintechColors.Primary else FintechColors.TextSecondary,
                        fontWeight = if (walletType == "monthly_budget") FontWeight.Bold else FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (walletType == "cold_wallet") FintechColors.Surface else FintechColors.SurfaceSubtle)
                        .clickable { walletType = "cold_wallet" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cold Wallet (Eksternal)",
                        style = FintechTypography.labelMedium,
                        color = if (walletType == "cold_wallet") FintechColors.Primary else FintechColors.TextSecondary,
                        fontWeight = if (walletType == "cold_wallet") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (walletType == "cold_wallet") {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (coldWalletType == "external_inflow") FintechColors.InflowGreenBg else FintechColors.SurfaceSubtle)
                            .clickable { coldWalletType = "external_inflow" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Dana Masuk (Profit)",
                            style = FintechTypography.labelMedium,
                            color = if (coldWalletType == "external_inflow") FintechColors.InflowGreen else FintechColors.TextSecondary,
                            fontWeight = if (coldWalletType == "external_inflow") FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (coldWalletType == "external_outflow") FintechColors.OutflowRedBg else FintechColors.SurfaceSubtle)
                            .clickable { coldWalletType = "external_outflow" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Dana Keluar (Aset)",
                            style = FintechTypography.labelMedium,
                            color = if (coldWalletType == "external_outflow") FintechColors.OutflowRed else FintechColors.TextSecondary,
                            fontWeight = if (coldWalletType == "external_outflow") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Nominal Input
            FintechTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) amountText = input
                },
                label = "Nominal (IDR)",
                placeholder = "0",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Nama Pengeluaran / Transaksi
            FintechTextField(
                value = name,
                onValueChange = { name = it },
                label = "Deskripsi / Nama",
                placeholder = if (walletType == "monthly_budget") "e.g. Nasi Padang, Paket Data" else "e.g. Reksadana, Beli SSD",
                leadingIcon = Icons.Rounded.Description
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector Chips
            Text(
                text = "Pilih Kategori",
                style = FintechTypography.labelMedium,
                color = FintechColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            val filteredCategories = if (walletType == "monthly_budget") {
                categories.filter { it.type == "expense" }
            } else {
                categories.filter { it.type != "expense" }
            }

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredCategories.forEach { cat ->
                    val isSelected = cat.id == selectedCategoryId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) FintechColors.Primary else FintechColors.SurfaceSubtle)
                            .clickable { selectedCategoryId = cat.id }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = cat.name,
                                style = FintechTypography.labelSmall,
                                color = if (isSelected) FintechColors.TextInverse else FintechColors.TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Quick Add Category Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(FintechColors.PrimaryLight)
                        .clickable { isAddCategorySheetVisible = true }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = FintechColors.Primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kategori Baru",
                            style = FintechTypography.labelSmall,
                            color = FintechColors.Primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Date Picker Field & Quick Chips
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                FintechTextField(
                    value = Formatters.formatDate(dateString),
                    onValueChange = {},
                    label = "Tanggal Transaksi",
                    leadingIcon = Icons.Rounded.CalendarToday,
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Clickable overlay to trigger native DatePickerDialog
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { openDatePicker() }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Date Selection Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isToday = dateString == todayString
                val isYesterday = dateString == yesterdayString

                // Hari Ini Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isToday) FintechColors.PrimaryLight else FintechColors.SurfaceSubtle)
                        .clickable { dateString = todayString }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "Hari Ini",
                        style = FintechTypography.labelSmall,
                        color = if (isToday) FintechColors.Primary else FintechColors.TextSecondary,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // Kemarin Chip
                if (yesterdayString.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isYesterday) FintechColors.PrimaryLight else FintechColors.SurfaceSubtle)
                            .clickable { dateString = yesterdayString }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Kemarin",
                            style = FintechTypography.labelSmall,
                            color = if (isYesterday) FintechColors.Primary else FintechColors.TextSecondary,
                            fontWeight = if (isYesterday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                // Pilih di Kalender Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!isToday && !isYesterday) FintechColors.PrimaryLight else FintechColors.SurfaceSubtle)
                        .clickable { openDatePicker() }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = if (!isToday && !isYesterday) FintechColors.Primary else FintechColors.TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (!isToday && !isYesterday) "Tanggal Lain" else "Pilih Tanggal",
                            style = FintechTypography.labelSmall,
                            color = if (!isToday && !isYesterday) FintechColors.Primary else FintechColors.TextSecondary,
                            fontWeight = if (!isToday && !isYesterday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FintechTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Catatan Tambahan (Opsional)",
                placeholder = "Catatan...",
                leadingIcon = Icons.Rounded.Edit
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action
            FintechButton(
                text = "Simpan Transaksi",
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0 || name.isBlank()) return@FintechButton

                    coroutineScope.launch {
                        val transactionType = if (walletType == "monthly_budget") {
                            "expense"
                        } else {
                            coldWalletType
                        }

                        financeRepository.addTransaction(
                            userId = userId,
                            name = name.trim(),
                            amount = amount,
                            transactionDate = dateString,
                            categoryId = selectedCategoryId,
                            walletType = walletType,
                            transactionType = transactionType,
                            notes = notes.ifBlank { null }
                        )

                        // Trigger sync in background
                        launch { financeRepository.performSync() }

                        onDismiss()
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (isAddCategorySheetVisible) {
        CategoryFormSheet(
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            userId = userId,
            defaultType = if (walletType == "monthly_budget") "expense" else "external_income",
            financeRepository = financeRepository,
            onDismiss = { isAddCategorySheetVisible = false },
            onCategorySaved = { newCat ->
                selectedCategoryId = newCat.id
            }
        )
    }
}
