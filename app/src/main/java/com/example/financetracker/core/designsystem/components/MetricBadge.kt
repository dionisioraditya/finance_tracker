package com.example.financetracker.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.designsystem.FintechColors
import com.example.financetracker.core.designsystem.FintechTypography

enum class MetricBadgeType {
    INFLOW,
    OUTFLOW,
    COLD_WALLET,
    WARNING,
    NEUTRAL
}

@Composable
fun MetricBadge(
    text: String,
    type: MetricBadgeType = MetricBadgeType.NEUTRAL,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (type) {
        MetricBadgeType.INFLOW -> FintechColors.InflowGreenBg to FintechColors.InflowGreen
        MetricBadgeType.OUTFLOW -> FintechColors.OutflowRedBg to FintechColors.OutflowRed
        MetricBadgeType.COLD_WALLET -> FintechColors.ColdWalletBlueBg to FintechColors.ColdWalletBlue
        MetricBadgeType.WARNING -> FintechColors.WarningAmberBg to FintechColors.WarningAmber
        MetricBadgeType.NEUTRAL -> FintechColors.SurfaceSubtle to FintechColors.TextSecondary
    }

    Box(
        modifier = modifier
            .background(color = bgColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = FintechTypography.labelSmall,
            color = textColor
        )
    }
}
