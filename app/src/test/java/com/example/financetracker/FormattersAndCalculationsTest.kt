package com.example.financetracker

import com.example.financetracker.core.util.Formatters
import org.junit.Assert.assertEquals
import org.junit.Test

class FormattersAndCalculationsTest {

    @Test
    fun testCurrencyFormatting() {
        val totalColdWallet = 1522489.0
        val monthlyRemaining = 57998.0
        val totalBudget = 2500000.0

        assertEquals("IDR 1.522.489", Formatters.formatCurrency(totalColdWallet))
        assertEquals("IDR 57.998", Formatters.formatCurrency(monthlyRemaining))
        assertEquals("IDR 2.500.000", Formatters.formatCurrency(totalBudget))
    }

    @Test
    fun testColdWalletFormulaExactMath() {
        // September 2026 spreadsheet numbers:
        val accumulatedMonthlyRemaining = 57998.0
        val externalInflow = 2309000.0 + 228150.0 + 1232341.0 // Reksadana + Gaji asdos + Transfer ortu = 3,769,491
        val externalOutflow = 2305000.0 // Beli SSD 1 TB

        val netExternalProfit = externalInflow - externalOutflow
        val totalColdWallet = accumulatedMonthlyRemaining + netExternalProfit

        assertEquals(1464491.0, netExternalProfit, 0.001)
        assertEquals(1522489.0, totalColdWallet, 0.001)
    }

    @Test
    fun testMonthFormatting() {
        val monthString = "2026-09"
        val formatted = Formatters.formatMonthName(monthString)
        assertEquals("September 2026", formatted)
    }
}
