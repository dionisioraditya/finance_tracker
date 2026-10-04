package com.example.financetracker.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["userId", "type"]),
        Index(value = ["isSynced"])
    ]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val type: String = "expense",
    val sortOrder: Int = 0,
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "monthly_budgets",
    indices = [
        Index(value = ["userId", "month"], unique = true),
        Index(value = ["isSynced"])
    ]
)
data class MonthlyBudgetEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val month: String,
    val totalBudget: Double = 0.0,
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "category_budgets",
    indices = [
        Index(value = ["monthlyBudgetId", "categoryId"], unique = true),
        Index(value = ["userId", "monthlyBudgetId"]),
        Index(value = ["isSynced"])
    ]
)
data class CategoryBudgetEntity(
    @PrimaryKey val id: String,
    val monthlyBudgetId: String,
    val categoryId: String,
    val userId: Long,
    val allocatedAmount: Double = 0.0,
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["userId", "walletType", "transactionDate"]),
        Index(value = ["userId", "transactionDate"]),
        Index(value = ["isSynced"])
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val categoryId: String? = null,
    val walletType: String = "monthly_budget", // monthly_budget, cold_wallet
    val transactionType: String = "expense", // expense, external_inflow, external_outflow, rollover_to_cold_wallet
    val name: String,
    val amount: Double,
    val transactionDate: String, // YYYY-MM-DD
    val receiptUrl: String? = null,
    val notes: String? = null,
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)

@Entity(
    tableName = "budget_rollovers",
    indices = [
        Index(value = ["userId", "month"], unique = true),
        Index(value = ["isSynced"])
    ]
)
data class BudgetRolloverEntity(
    @PrimaryKey val id: String,
    val userId: Long,
    val month: String,
    val remainingAmount: Double = 0.0,
    val isTransferred: Boolean = false,
    val transferredAt: String? = null,
    val isSynced: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false
)
