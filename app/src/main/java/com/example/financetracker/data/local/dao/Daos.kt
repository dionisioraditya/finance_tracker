package com.example.financetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financetracker.data.local.entity.BudgetRolloverEntity
import com.example.financetracker.data.local.entity.CategoryBudgetEntity
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.local.entity.MonthlyBudgetEntity
import com.example.financetracker.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getCategories(userId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isSynced = 0")
    suspend fun getUnsyncedCategories(): List<CategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Query("UPDATE categories SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long): Int

    @Query("UPDATE categories SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>): Int
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM monthly_budgets WHERE userId = :userId AND month = :month AND isDeleted = 0 LIMIT 1")
    fun getMonthlyBudget(userId: Long, month: String): Flow<MonthlyBudgetEntity?>

    @Query("SELECT * FROM monthly_budgets WHERE userId = :userId AND month = :month AND isDeleted = 0 LIMIT 1")
    suspend fun getMonthlyBudgetSync(userId: Long, month: String): MonthlyBudgetEntity?

    @Query("SELECT * FROM monthly_budgets WHERE userId = :userId AND isDeleted = 0")
    suspend fun getAllMonthlyBudgetsSync(userId: Long): List<MonthlyBudgetEntity>

    @Query("SELECT * FROM category_budgets WHERE monthlyBudgetId = :monthlyBudgetId AND isDeleted = 0")
    fun getCategoryBudgets(monthlyBudgetId: String): Flow<List<CategoryBudgetEntity>>

    @Query("SELECT * FROM category_budgets WHERE monthlyBudgetId = :monthlyBudgetId AND isDeleted = 0")
    suspend fun getCategoryBudgetsSync(monthlyBudgetId: String): List<CategoryBudgetEntity>

    @Query("SELECT * FROM monthly_budgets WHERE isSynced = 0")
    suspend fun getUnsyncedMonthlyBudgets(): List<MonthlyBudgetEntity>

    @Query("SELECT * FROM category_budgets WHERE isSynced = 0")
    suspend fun getUnsyncedCategoryBudgets(): List<CategoryBudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMonthlyBudget(budget: MonthlyBudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategoryBudgets(allocations: List<CategoryBudgetEntity>)

    @Query("UPDATE monthly_budgets SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markMonthlyBudgetsAsSynced(ids: List<String>): Int

    @Query("UPDATE category_budgets SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markCategoryBudgetsAsSynced(ids: List<String>): Int
}

@Dao
interface TransactionDao {
    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND walletType = 'monthly_budget' 
        AND transactionType = 'expense' 
        AND transactionDate BETWEEN :startDate AND :endDate 
        AND isDeleted = 0 
        ORDER BY transactionDate DESC, updatedAt DESC
    """)
    fun getMonthlyExpenses(userId: Long, startDate: String, endDate: String): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND walletType = 'cold_wallet' 
        AND isDeleted = 0 
        ORDER BY transactionDate DESC, updatedAt DESC
    """)
    fun getColdWalletMutations(userId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY transactionDate DESC, updatedAt DESC LIMIT :limit")
    fun getRecentTransactions(userId: Long, limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND isDeleted = 0 ORDER BY transactionDate DESC, updatedAt DESC")
    fun getAllTransactions(userId: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE userId = :userId 
        AND walletType = 'monthly_budget' 
        AND transactionType = 'expense' 
        AND transactionDate BETWEEN :startDate AND :endDate 
        AND isDeleted = 0
    """)
    suspend fun getMonthlyExpensesSync(userId: Long, startDate: String, endDate: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getUnsyncedTransactions(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Query("UPDATE transactions SET isDeleted = 1, isSynced = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long): Int

    @Query("UPDATE transactions SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>): Int
}

@Dao
interface RolloverDao {
    @Query("SELECT * FROM budget_rollovers WHERE userId = :userId AND isDeleted = 0 ORDER BY month DESC")
    fun getRollovers(userId: Long): Flow<List<BudgetRolloverEntity>>

    @Query("SELECT * FROM budget_rollovers WHERE userId = :userId AND month = :month AND isDeleted = 0 LIMIT 1")
    suspend fun getRolloverForMonthSync(userId: Long, month: String): BudgetRolloverEntity?

    @Query("SELECT * FROM budget_rollovers WHERE isSynced = 0")
    suspend fun getUnsyncedRollovers(): List<BudgetRolloverEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRollovers(rollovers: List<BudgetRolloverEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRollover(rollover: BudgetRolloverEntity)

    @Query("UPDATE budget_rollovers SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<String>): Int
}
