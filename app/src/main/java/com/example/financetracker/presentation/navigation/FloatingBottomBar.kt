package com.example.financetracker.presentation.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.financetracker.core.designsystem.FintechColors

@Composable
fun FloatingBottomBar(
    pagerState: PagerState,
    isSubscreenOpen: Boolean = false,
    onScreenSelected: (Screen) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0x330F172A),
                ambientColor = Color(0x1A0F172A)
            )
            .border(
                width = 1.dp,
                color = FintechColors.BorderHairline.copy(alpha = 0.9f),
                shape = RoundedCornerShape(32.dp)
            ),
        shape = RoundedCornerShape(32.dp),
        color = FintechColors.Surface,
        tonalElevation = 6.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            val totalWidth = maxWidth
            val slotWidth = totalWidth / 5

            // Row containing the 5 slots with equal width (weight 1f)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Slot 0: Dashboard
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingBottomNavIcon(
                        screen = Screen.Dashboard,
                        isSelected = pagerState.currentPage == 0 && !isSubscreenOpen,
                        onClick = { onScreenSelected(Screen.Dashboard) }
                    )
                }

                // Slot 1: Cold Wallet
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingBottomNavIcon(
                        screen = Screen.ColdWallet,
                        isSelected = pagerState.currentPage == 1 && !isSubscreenOpen,
                        onClick = { onScreenSelected(Screen.ColdWallet) }
                    )
                }

                // Slot 2: Center Prominent Action Button (+)
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = FintechColors.Primary,
                                ambientColor = FintechColors.Primary
                            )
                            .clip(CircleShape)
                            .background(FintechColors.Primary)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true, color = Color.White.copy(alpha = 0.35f))
                            ) { onAddClick() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Catat Transaksi",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Slot 3: Riwayat Transaksi
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingBottomNavIcon(
                        screen = Screen.Transactions,
                        isSelected = pagerState.currentPage == 2 && !isSubscreenOpen,
                        onClick = { onScreenSelected(Screen.Transactions) }
                    )
                }

                // Slot 4: Pengaturan
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingBottomNavIcon(
                        screen = Screen.Settings,
                        isSelected = (pagerState.currentPage == 3) || isSubscreenOpen,
                        onClick = { onScreenSelected(Screen.Settings) }
                    )
                }
            }

            // Real-time Smooth Sliding Blue Active Indicator Line (Perfect Center Alignment)
            val rawPage = if (!isSubscreenOpen) {
                (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 3f)
            } else {
                3f
            }

            val activeSlot = when {
                rawPage <= 1f -> rawPage
                rawPage <= 2f -> 1f + (rawPage - 1f) * 2f
                else -> 3f + (rawPage - 2f)
            }

            val indicatorWidth = 24.dp
            val indicatorOffset = (slotWidth * (activeSlot + 0.5f)) - (indicatorWidth / 2f)

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = indicatorOffset)
                    .padding(bottom = 1.dp)
                    .width(indicatorWidth)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(FintechColors.Primary)
            )
        }
    }
}

@Composable
private fun FloatingBottomNavIcon(
    screen: Screen,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) FintechColors.Primary else FintechColors.TextMuted,
        animationSpec = tween(durationMillis = 200),
        label = "iconTint"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true, color = FintechColors.PrimaryLight)
            ) { onClick() }
    ) {
        screen.icon?.let { icon ->
            Icon(
                imageVector = icon,
                contentDescription = screen.title,
                tint = iconTint,
                modifier = Modifier.size(23.dp)
            )
        }
    }
}
