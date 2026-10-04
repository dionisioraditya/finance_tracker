package com.example.financetracker.presentation.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.financetracker.core.designsystem.CategoryIcons
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography
import com.example.financetracker.core.designsystem.components.FintechButton
import com.example.financetracker.core.designsystem.components.FintechCard
import com.example.financetracker.core.designsystem.components.FintechTextField
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.repository.FinanceRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CategoryFormSheet(
    sheetState: SheetState,
    userId: Long,
    category: CategoryEntity? = null,
    defaultType: String = "expense",
    financeRepository: FinanceRepository,
    onDismiss: () -> Unit,
    onCategorySaved: ((CategoryEntity) -> Unit)? = null
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedType by remember { mutableStateOf(category?.type ?: defaultType) }
    var selectedIconKey by remember { mutableStateOf(category?.icon ?: "category") }
    var selectedColorHex by remember { mutableStateOf(category?.color ?: "#1E60F2") }
    var isSaving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    text = if (category == null) "Tambah Kategori Baru" else "Ubah Kategori",
                    style = FintechTypography.titleLarge,
                    color = FintechColors.TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Tutup", tint = FintechColors.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Preview Card
            val activeColor = CategoryIcons.getColor(selectedColorHex)
            val activeSubtleColor = CategoryIcons.getSubtleColor(selectedColorHex)
            val activeIcon = CategoryIcons.getIcon(selectedIconKey)

            FintechCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = 14.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(activeSubtleColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = activeIcon,
                            contentDescription = null,
                            tint = activeColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (name.isNotBlank()) name else "Nama Kategori",
                            style = FintechTypography.titleMedium,
                            color = if (name.isNotBlank()) FintechColors.TextPrimary else FintechColors.TextMuted
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (selectedType == "expense") "Pengeluaran Bulanan" else "Cold Wallet & Aset",
                            style = FintechTypography.bodySmall,
                            color = FintechColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Category Type Selector
            Text(
                text = "Tipe Kategori",
                style = FintechTypography.labelMedium,
                color = FintechColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

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
                        .background(if (selectedType == "expense") FintechColors.Surface else FintechColors.SurfaceSubtle)
                        .clickable { selectedType = "expense" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pengeluaran Bulanan",
                        style = FintechTypography.labelMedium,
                        color = if (selectedType == "expense") FintechColors.Primary else FintechColors.TextSecondary,
                        fontWeight = if (selectedType == "expense") FontWeight.Bold else FontWeight.Normal
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedType != "expense") FintechColors.Surface else FintechColors.SurfaceSubtle)
                        .clickable { selectedType = "external_income" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cold Wallet & Aset",
                        style = FintechTypography.labelMedium,
                        color = if (selectedType != "expense") FintechColors.Primary else FintechColors.TextSecondary,
                        fontWeight = if (selectedType != "expense") FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Name Field
            FintechTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = "Nama Kategori",
                placeholder = if (selectedType == "expense") "e.g. Kesehatan, Hobi, Donasi" else "e.g. Dividen, Reksadana, Gadget"
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = errorMessage.orEmpty(),
                    style = FintechTypography.bodySmall,
                    color = FintechColors.OutflowRed
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Icon Picker
            Text(
                text = "Pilih Ikon Vektor",
                style = FintechTypography.labelMedium,
                color = FintechColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CategoryIcons.ICONS.forEach { item ->
                    val isSelected = item.key.equals(selectedIconKey, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) activeSubtleColor else FintechColors.SurfaceSubtle)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) activeColor else FintechColors.BorderHairline,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedIconKey = item.key },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = if (isSelected) activeColor else FintechColors.TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Color Picker
            Text(
                text = "Pilih Warna Aksen",
                style = FintechTypography.labelMedium,
                color = FintechColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryIcons.COLORS.forEach { opt ->
                    val isSelected = opt.hex.equals(selectedColorHex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(opt.color)
                            .clickable { selectedColorHex = opt.hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = "Terpilih",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            FintechButton(
                text = if (category == null) "Simpan Kategori Baru" else "Perbarui Kategori",
                isLoading = isSaving,
                onClick = {
                    if (name.isBlank()) {
                        errorMessage = "Nama kategori tidak boleh kosong"
                        return@FintechButton
                    }
                    coroutineScope.launch {
                        isSaving = true
                        val saved = financeRepository.saveCategory(
                            id = category?.id,
                            userId = userId,
                            name = name,
                            icon = selectedIconKey,
                            color = selectedColorHex,
                            type = selectedType
                        )
                        launch { financeRepository.performSync() }
                        isSaving = false
                        onCategorySaved?.invoke(saved)
                        onDismiss()
                    }
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
