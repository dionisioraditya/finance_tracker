package com.example.financetracker.data.repository

import com.example.financetracker.core.database.FinanceDatabase
import com.example.financetracker.core.datastore.UserPreferences
import com.example.financetracker.data.local.entity.BudgetRolloverEntity
import com.example.financetracker.data.local.entity.CategoryBudgetEntity
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.local.entity.MonthlyBudgetEntity
import com.example.financetracker.data.local.entity.TransactionEntity
import com.example.financetracker.data.remote.api.ApiClient
import com.example.financetracker.data.remote.dto.BudgetRolloverDto
import com.example.financetracker.data.remote.dto.CategoryBudgetDto
import com.example.financetracker.data.remote.dto.CategoryDto
import com.example.financetracker.data.remote.dto.MonthlyBudgetDto
import com.example.financetracker.data.remote.dto.SyncPushPayload
import com.example.financetracker.data.remote.dto.SyncRequest
import com.example.financetracker.data.remote.dto.TransactionDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

class FinanceRepository(
    private val database: FinanceDatabase,
    private val apiClient: ApiClient,
    private val userPreferences: UserPreferences
) {
    private val categoryDao = database.categoryDao()
    private val budgetDao = database.budgetDao()
    private val transactionDao = database.transactionDao()
    private val rolloverDao = database.rolloverDao()

    // Reactive Reads from Room (Single Source of Truth)
    fun getCategories(userId: Long): Flow<List<CategoryEntity>> = categoryDao.getCategories(userId)

    fun getMonthlyExpenses(userId: Long, month: String): Flow<List<TransactionEntity>> {
        val startDate = "$month-01"
        val parsed = LocalDate.parse(startDate)
        val endDate = parsed.withDayOfMonth(parsed.lengthOfMonth()).toString()
        return transactionDao.getMonthlyExpenses(userId, startDate, endDate)
    }

    fun getRecentTransactions(userId: Long, limit: Int = 10): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(userId, limit)

    fun getAllTransactions(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions(userId)

    fun getColdWalletMutations(userId: Long): Flow<List<TransactionEntity>> =
        transactionDao.getColdWalletMutations(userId)

    fun getMonthlyBudget(userId: Long, month: String): Flow<MonthlyBudgetEntity?> =
        budgetDao.getMonthlyBudget(userId, month)

    fun getCategoryBudgets(monthlyBudgetId: String): Flow<List<CategoryBudgetEntity>> =
        budgetDao.getCategoryBudgets(monthlyBudgetId)

    fun getRollovers(userId: Long): Flow<List<BudgetRolloverEntity>> =
        rolloverDao.getRollovers(userId)

    suspend fun saveCategory(
        id: String? = null,
        userId: Long,
        name: String,
        icon: String? = null,
        color: String? = null,
        type: String = "expense"
    ): CategoryEntity = withContext(Dispatchers.IO) {
        val categoryId = id ?: UUID.randomUUID().toString()
        val entity = CategoryEntity(
            id = categoryId,
            userId = userId,
            name = name.trim(),
            icon = icon,
            color = color,
            type = type,
            sortOrder = 0,
            isSynced = false,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        categoryDao.insertCategory(entity)
        entity
    }

    suspend fun deleteCategory(id: String) = withContext(Dispatchers.IO) {
        categoryDao.softDelete(id, System.currentTimeMillis())
    }

    // Offline-First Writes (Instant Room Save)
    suspend fun addTransaction(
        userId: Long,
        name: String,
        amount: Double,
        transactionDate: String,
        categoryId: String? = null,
        walletType: String = "monthly_budget",
        transactionType: String = "expense",
        notes: String? = null
    ) = withContext(Dispatchers.IO) {
        val entity = TransactionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            categoryId = categoryId,
            walletType = walletType,
            transactionType = transactionType,
            name = name,
            amount = amount,
            transactionDate = transactionDate,
            notes = notes,
            isSynced = false,
            updatedAt = System.currentTimeMillis(),
            isDeleted = false
        )
        transactionDao.insertTransaction(entity)
        if (walletType == "monthly_budget" && transactionType == "expense") {
            val month = transactionDate.take(7)
            recomputeRolloverForMonth(userId, month)
        }
    }

    suspend fun deleteTransaction(id: String) = withContext(Dispatchers.IO) {
        transactionDao.softDelete(id, System.currentTimeMillis())
    }

    suspend fun saveMonthlyBudget(
        userId: Long,
        month: String,
        totalBudget: Double,
        allocations: Map<String, Double>
    ) = withContext(Dispatchers.IO) {
        val existing = budgetDao.getMonthlyBudgetSync(userId, month)
        val budgetId = existing?.id ?: UUID.randomUUID().toString()
        val budgetEntity = MonthlyBudgetEntity(
            id = budgetId,
            userId = userId,
            month = month,
            totalBudget = totalBudget,
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        budgetDao.upsertMonthlyBudget(budgetEntity)

        val existingAllocations = budgetDao.getCategoryBudgetsSync(budgetId).associateBy { it.categoryId }

        val allocationEntities = allocations.map { (catId, amount) ->
            val existingAlloc = existingAllocations[catId]
            CategoryBudgetEntity(
                id = existingAlloc?.id ?: UUID.randomUUID().toString(),
                monthlyBudgetId = budgetId,
                categoryId = catId,
                userId = userId,
                allocatedAmount = amount,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
        }
        budgetDao.upsertCategoryBudgets(allocationEntities)
        recomputeRolloverForMonth(userId, month)
    }

    suspend fun recomputeRolloverForMonth(userId: Long, month: String) = withContext(Dispatchers.IO) {
        val budget = budgetDao.getMonthlyBudgetSync(userId, month) ?: return@withContext
        val startOfMonth = "$month-01"
        val endOfMonth = "$month-31"
        val expenses = transactionDao.getMonthlyExpensesSync(userId, startOfMonth, endOfMonth)
        val totalSpent = expenses.sumOf { it.amount }
        val remaining = maxOf(0.0, budget.totalBudget - totalSpent)

        val existing = rolloverDao.getRolloverForMonthSync(userId, month)
        if (existing == null) {
            val entity = BudgetRolloverEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                month = month,
                remainingAmount = remaining,
                isTransferred = false,
                transferredAt = null,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
            rolloverDao.upsertRollover(entity)
        } else if (!existing.isTransferred) {
            val updated = existing.copy(
                remainingAmount = remaining,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
            rolloverDao.upsertRollover(updated)
        }
    }

    suspend fun syncRolloversWithBudgets(userId: Long) = withContext(Dispatchers.IO) {
        val budgets = budgetDao.getAllMonthlyBudgetsSync(userId)
        budgets.forEach { budget ->
            recomputeRolloverForMonth(userId, budget.month)
        }
    }

    suspend fun toggleRolloverTransfer(rollover: BudgetRolloverEntity) = withContext(Dispatchers.IO) {
        val newStatus = !rollover.isTransferred
        val updated = rollover.copy(
            isTransferred = newStatus,
            transferredAt = if (newStatus) LocalDate.now().toString() else null,
            isSynced = false,
            updatedAt = System.currentTimeMillis()
        )
        rolloverDao.upsertRollover(updated)
    }

    // 2-Way Sync Engine (Push Local Changes -> Pull Server Updates)
    suspend fun performSync(): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val lastSync = userPreferences.lastSyncTimestamp.firstOrNull()

            // 1. Gather unsynced local data to push
            val unsyncedCategories = categoryDao.getUnsyncedCategories()
            val unsyncedBudgets = budgetDao.getUnsyncedMonthlyBudgets()
            val unsyncedCategoryBudgets = budgetDao.getUnsyncedCategoryBudgets()
            val unsyncedTransactions = transactionDao.getUnsyncedTransactions()
            val unsyncedRollovers = rolloverDao.getUnsyncedRollovers()

            val deleteTimestamp = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(java.util.Date())

            val pushPayload = SyncPushPayload(
                categories = unsyncedCategories.map {
                    CategoryDto(it.id, it.userId, it.name, it.icon, it.color, it.type, it.sortOrder, if (it.isDeleted) deleteTimestamp else null)
                },
                budgets = unsyncedBudgets.map { b ->
                    val allocs = budgetDao.getCategoryBudgetsSync(b.id)
                    MonthlyBudgetDto(
                        id = b.id,
                        month = b.month,
                        totalBudget = b.totalBudget,
                        allocations = allocs.map { a ->
                            CategoryBudgetDto(
                                id = a.id,
                                monthlyBudgetId = b.id,
                                categoryId = a.categoryId,
                                allocatedAmount = a.allocatedAmount,
                                deletedAt = if (a.isDeleted) deleteTimestamp else null
                            )
                        },
                        deletedAt = if (b.isDeleted) deleteTimestamp else null
                    )
                },
                transactions = unsyncedTransactions.map {
                    TransactionDto(it.id, it.categoryId, it.walletType, it.transactionType, it.name, it.amount, it.transactionDate, it.receiptUrl, it.notes, if (it.isDeleted) deleteTimestamp else null)
                },
                rollovers = unsyncedRollovers.map {
                    BudgetRolloverDto(it.id, it.month, it.remainingAmount, it.isTransferred, it.transferredAt, if (it.isDeleted) deleteTimestamp else null)
                }
            )

            val syncRequest = SyncRequest(
                lastSyncTimestamp = lastSync,
                push = pushPayload
            )

            // 2. Call server sync
            val syncResponse = apiClient.sync(syncRequest).getOrThrow()

            // 3. Mark pushed items as synced in Room
            if (unsyncedCategories.isNotEmpty()) categoryDao.markAsSynced(unsyncedCategories.map { it.id })
            if (unsyncedBudgets.isNotEmpty()) budgetDao.markMonthlyBudgetsAsSynced(unsyncedBudgets.map { it.id })
            if (unsyncedCategoryBudgets.isNotEmpty()) budgetDao.markCategoryBudgetsAsSynced(unsyncedCategoryBudgets.map { it.id })
            if (unsyncedTransactions.isNotEmpty()) transactionDao.markAsSynced(unsyncedTransactions.map { it.id })
            if (unsyncedRollovers.isNotEmpty()) rolloverDao.markAsSynced(unsyncedRollovers.map { it.id })

            // 4. Save pulled items from server into Room
            val pull = syncResponse.pull
            val currentUserId = userPreferences.userId.firstOrNull() ?: 1L

            if (pull.categories.isNotEmpty()) {
                val pulledEntities = pull.categories.map {
                    CategoryEntity(it.id, it.userId ?: currentUserId, it.name, it.icon, it.color, it.type, it.sortOrder, isSynced = true, isDeleted = it.deletedAt != null)
                }
                categoryDao.upsertCategories(pulledEntities)
            }

            if (pull.monthlyBudgets.isNotEmpty()) {
                val pulledBudgets = pull.monthlyBudgets.map {
                    MonthlyBudgetEntity(it.id ?: UUID.randomUUID().toString(), currentUserId, it.month, it.totalBudget, isSynced = true, isDeleted = it.deletedAt != null)
                }
                pulledBudgets.forEach { budgetDao.upsertMonthlyBudget(it) }
            }

            if (pull.categoryBudgets.isNotEmpty()) {
                val pulledAllocs = pull.categoryBudgets.mapNotNull {
                    val mId = it.monthlyBudgetId ?: return@mapNotNull null
                    CategoryBudgetEntity(
                        id = it.id ?: UUID.randomUUID().toString(),
                        monthlyBudgetId = mId,
                        categoryId = it.categoryId,
                        userId = currentUserId,
                        allocatedAmount = it.allocatedAmount,
                        isSynced = true,
                        isDeleted = it.deletedAt != null
                    )
                }
                budgetDao.upsertCategoryBudgets(pulledAllocs)
            }

            if (pull.transactions.isNotEmpty()) {
                val pulledTx = pull.transactions.map {
                    TransactionEntity(it.id, currentUserId, it.categoryId, it.walletType, it.transactionType, it.name, it.amount, it.transactionDate, it.receiptUrl, it.notes, isSynced = true, isDeleted = it.deletedAt != null)
                }
                transactionDao.upsertTransactions(pulledTx)
            }

            if (pull.rollovers.isNotEmpty()) {
                val pulledRollovers = pull.rollovers.map {
                    BudgetRolloverEntity(it.id, currentUserId, it.month, it.remainingAmount, it.isTransferred, it.transferredAt, isSynced = true, isDeleted = it.deletedAt != null)
                }
                rolloverDao.upsertRollovers(pulledRollovers)
            }

            // 5. Update last sync timestamp in DataStore
            userPreferences.saveLastSync(syncResponse.serverTime)
        }
    }
}
