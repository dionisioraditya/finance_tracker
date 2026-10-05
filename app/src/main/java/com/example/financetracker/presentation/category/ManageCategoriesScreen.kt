package com.example.financetracker.presentation.category

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import com.example.financetracker.core.designsystem.CategoryIcons
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(
    userId: Long,
    financeRepository: FinanceRepository,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pengeluaran Bulanan, 1: Cold Wallet & Aset
    var isSheetOpen by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val categories by financeRepository.getCategories(userId).collectAsState(initial = emptyList())
    val expenseCategories = categories.filter { it.type == "expense" }
    val coldWalletCategories = categories.filter { it.type != "expense" }
    val displayCategories = if (selectedTab == 0) expenseCategories else coldWalletCategories

    Scaffold(
//        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        containerColor = FintechColors.PrimaryLight,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    isSheetOpen = true
                },
                containerColor = FintechColors.Primary,
                contentColor = FintechColors.TextInverse,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 76.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Tambah Kategori")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Back Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FintechColors.SurfaceSubtle)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = FintechColors.TextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Kelola Kategori",
                            style = FintechTypography.headlineMedium,
                            color = FintechColors.TextPrimary
                        )
                        Text(
                            text = "Atur pos pengeluaran & sumber aset",
                            style = FintechTypography.bodyMedium,
                            color = FintechColors.TextSecondary
                        )
                    }
                }
            }

            // Tab Selector (Pengeluaran Bulanan vs Cold Wallet)
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
                            text = "Anggaran Bulanan (${expenseCategories.size})",
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
                            text = "Cold Wallet (${coldWalletCategories.size})",
                            style = FintechTypography.labelMedium,
                            color = if (selectedTab == 1) FintechColors.Primary else FintechColors.TextSecondary,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Category List
            if (displayCategories.isEmpty()) {
                item {
                    FintechCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Belum ada kategori pada grup ini. Tekan (+) untuk menambahkan.",
                            style = FintechTypography.bodyMedium,
                            color = FintechColors.TextSecondary
                        )
                    }
                }
            } else {
                items(displayCategories, key = { it.id }) { cat ->
                    val color = CategoryIcons.getColor(cat.color)
                    val subtleColor = CategoryIcons.getSubtleColor(cat.color)
                    val icon = CategoryIcons.getIcon(cat.icon)

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
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(subtleColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(22.dp)
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
                                        text = if (cat.type == "expense") "Anggaran Bulanan" else "Cold Wallet",
                                        style = FintechTypography.bodySmall,
                                        color = FintechColors.TextSecondary
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        editingCategory = cat
                                        isSheetOpen = true
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "Edit Kategori",
                                        tint = FintechColors.TextSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        categoryToDelete = cat
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "Hapus Kategori",
                                        tint = FintechColors.OutflowRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Modal Form Sheet
    if (isSheetOpen) {
        CategoryFormSheet(
            sheetState = sheetState,
            userId = userId,
            category = editingCategory,
            defaultType = if (selectedTab == 0) "expense" else "external_income",
            financeRepository = financeRepository,
            onDismiss = {
                isSheetOpen = false
                editingCategory = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (categoryToDelete != null) {
        val target = categoryToDelete!!
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = {
                Text(
                    text = "Hapus Kategori?",
                    style = FintechTypography.titleLarge,
                    color = FintechColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "Apakah Anda yakin ingin menghapus kategori '${target.name}'? Histori transaksi lama yang menggunakan kategori ini tetap tersimpan dengan aman.",
                    style = FintechTypography.bodyMedium,
                    color = FintechColors.TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            financeRepository.deleteCategory(target.id)
                            launch { financeRepository.performSync() }
                            categoryToDelete = null
                        }
                    }
                ) {
                    Text("Hapus", color = FintechColors.OutflowRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Batal", color = FintechColors.TextSecondary)
                }
            },
            containerColor = FintechColors.Surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
