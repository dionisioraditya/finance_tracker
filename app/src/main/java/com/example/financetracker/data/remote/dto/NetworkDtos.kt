package com.example.financetracker.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerialName("password_confirmation") val passwordConfirmation: String? = null
)

@Serializable
data class UserDto(
    val id: Long,
    val name: String,
    val email: String,
    val role: String? = null
)

@Serializable
data class AuthResponse(
    val message: String,
    val token: String? = null,
    val user: UserDto? = null
)

@Serializable
data class CategoryDto(
    val id: String,
    @SerialName("user_id") val userId: Long? = null,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    val type: String = "expense",
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("deleted_at") val deletedAt: String? = null
)

@Serializable
data class CategoryBudgetDto(
    val id: String? = null,
    @SerialName("monthly_budget_id") val monthlyBudgetId: String? = null,
    @SerialName("category_id") val categoryId: String,
    @SerialName("allocated_amount") val allocatedAmount: Double,
    @SerialName("deleted_at") val deletedAt: String? = null
)

@Serializable
data class MonthlyBudgetDto(
    val id: String? = null,
    val month: String,
    @SerialName("total_budget") val totalBudget: Double,
    val allocations: List<CategoryBudgetDto> = emptyList(),
    @SerialName("deleted_at") val deletedAt: String? = null
)

@Serializable
data class CategoryBreakdownDto(
    @SerialName("category_id") val categoryId: String,
    val name: String,
    val icon: String? = null,
    val color: String? = null,
    @SerialName("allocated_amount") val allocatedAmount: Double,
    val spending: Double,
    val remaining: Double,
    val percentage: Double
)

@Serializable
data class BudgetSummaryResponse(
    val month: String,
    @SerialName("monthly_budget_id") val monthlyBudgetId: String? = null,
    @SerialName("total_budget") val totalBudget: Double,
    @SerialName("total_spending") val totalSpending: Double,
    val remaining: Double,
    val categories: List<CategoryBreakdownDto> = emptyList()
)

@Serializable
data class TransactionDto(
    val id: String,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("wallet_type") val walletType: String = "monthly_budget",
    @SerialName("transaction_type") val transactionType: String = "expense",
    val name: String,
    val amount: Double,
    @SerialName("transaction_date") val transactionDate: String,
    @SerialName("receipt_url") val receiptUrl: String? = null,
    val notes: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)

@Serializable
data class BudgetRolloverDto(
    val id: String,
    val month: String,
    @SerialName("remaining_amount") val remainingAmount: Double,
    @SerialName("is_transferred") val isTransferred: Boolean = false,
    @SerialName("transferred_at") val transferredAt: String? = null,
    @SerialName("deleted_at") val deletedAt: String? = null
)

@Serializable
data class ColdWalletSummaryDto(
    @SerialName("total_cold_wallet_balance") val totalColdWalletBalance: Double,
    @SerialName("accumulated_monthly_remaining") val accumulatedMonthlyRemaining: Double,
    @SerialName("total_external_profit_net") val totalExternalProfitNet: Double,
    @SerialName("external_inflow") val externalInflow: Double,
    @SerialName("external_outflow") val externalOutflow: Double
)

@Serializable
data class SyncPushPayload(
    val categories: List<CategoryDto> = emptyList(),
    val budgets: List<MonthlyBudgetDto> = emptyList(),
    val transactions: List<TransactionDto> = emptyList(),
    val rollovers: List<BudgetRolloverDto> = emptyList()
)

@Serializable
data class SyncRequest(
    @SerialName("last_sync_timestamp") val lastSyncTimestamp: String? = null,
    val push: SyncPushPayload = SyncPushPayload()
)

@Serializable
data class SyncPullPayload(
    val categories: List<CategoryDto> = emptyList(),
    @SerialName("monthly_budgets") val monthlyBudgets: List<MonthlyBudgetDto> = emptyList(),
    @SerialName("category_budgets") val categoryBudgets: List<CategoryBudgetDto> = emptyList(),
    val transactions: List<TransactionDto> = emptyList(),
    val rollovers: List<BudgetRolloverDto> = emptyList()
)

@Serializable
data class SyncResponse(
    @SerialName("server_time") val serverTime: String,
    val pull: SyncPullPayload = SyncPullPayload()
)
