package com.example.financetracker.core.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

data class CategoryColorOption(
    val hex: String,
    val name: String,
    val color: Color,
    val subtleColor: Color
)

object CategoryIcons {
    val ICONS = listOf(
        CategoryIconOption("restaurant", "Makan", Icons.Rounded.Restaurant),
        CategoryIconOption("local_cafe", "Kafe", Icons.Rounded.LocalCafe),
        CategoryIconOption("shopping_bag", "Belanja", Icons.Rounded.ShoppingBag),
        CategoryIconOption("directions_car", "Transport", Icons.Rounded.DirectionsCar),
        CategoryIconOption("build", "Infrastruktur", Icons.Rounded.Build),
        CategoryIconOption("medical_services", "Kesehatan", Icons.Rounded.MedicalServices),
        CategoryIconOption("school", "Pendidikan", Icons.Rounded.School),
        CategoryIconOption("sports_esports", "Hiburan", Icons.Rounded.SportsEsports),
        CategoryIconOption("fitness_center", "Olahraga", Icons.Rounded.FitnessCenter),
        CategoryIconOption("flight", "Travel", Icons.Rounded.Flight),
        CategoryIconOption("savings", "Tabungan", Icons.Rounded.Savings),
        CategoryIconOption("trending_up", "Investasi", Icons.Rounded.TrendingUp),
        CategoryIconOption("account_balance", "Aset", Icons.Rounded.AccountBalance),
        CategoryIconOption("work", "Kerja/Usaha", Icons.Rounded.Work),
        CategoryIconOption("category", "Lainnya", Icons.Rounded.Category)
    )

    val COLORS = listOf(
        CategoryColorOption("#1E60F2", "Blue", Color(0xFF1E60F2), Color(0xFFEBF1FF)),
        CategoryColorOption("#10B981", "Green", Color(0xFF10B981), Color(0xFFE6F9F0)),
        CategoryColorOption("#6366F1", "Indigo", Color(0xFF6366F1), Color(0xFFEEF0FD)),
        CategoryColorOption("#F59E0B", "Amber", Color(0xFFF59E0B), Color(0xFFFEF7E6)),
        CategoryColorOption("#F43F5E", "Rose", Color(0xFFF43F5E), Color(0xFFFEECEF)),
        CategoryColorOption("#8B5CF6", "Violet", Color(0xFF8B5CF6), Color(0xFFF3EEFD)),
        CategoryColorOption("#06B6D4", "Teal", Color(0xFF06B6D4), Color(0xFFE3F8FA)),
        CategoryColorOption("#64748B", "Slate", Color(0xFF64748B), Color(0xFFF1F5F9))
    )

    fun getIcon(key: String?): ImageVector {
        if (key.isNullOrBlank()) return Icons.Rounded.Category
        return when (key.lowercase()) {
            "restaurant", "food", "foods" -> Icons.Rounded.Restaurant
            "local_cafe", "cafe", "coffee" -> Icons.Rounded.LocalCafe
            "shopping_bag", "shopping", "wants" -> Icons.Rounded.ShoppingBag
            "directions_car", "transport", "transports", "car" -> Icons.Rounded.DirectionsCar
            "build", "infrastructure", "tools" -> Icons.Rounded.Build
            "medical_services", "health", "hospital" -> Icons.Rounded.MedicalServices
            "school", "education", "book" -> Icons.Rounded.School
            "sports_esports", "entertainment", "game" -> Icons.Rounded.SportsEsports
            "fitness_center", "gym", "sport" -> Icons.Rounded.FitnessCenter
            "flight", "travel", "plane" -> Icons.Rounded.Flight
            "savings", "saving" -> Icons.Rounded.Savings
            "trending_up", "investment", "reksadana", "stock" -> Icons.Rounded.TrendingUp
            "account_balance", "bank", "asset" -> Icons.Rounded.AccountBalance
            "work", "business", "side hustle" -> Icons.Rounded.Work
            else -> Icons.Rounded.Category
        }
    }

    fun getColor(hex: String?): Color {
        if (hex.isNullOrBlank()) return FintechColors.Primary
        return runCatching {
            val clean = hex.removePrefix("#")
            if (clean.length == 6) {
                Color(android.graphics.Color.parseColor("#$clean"))
            } else {
                FintechColors.Primary
            }
        }.getOrDefault(FintechColors.Primary)
    }

    fun getSubtleColor(hex: String?): Color {
        val baseColor = getColor(hex)
        return baseColor.copy(alpha = 0.12f)
    }
}
