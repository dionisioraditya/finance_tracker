package com.example.financetracker.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.financetracker.data.local.dao.BudgetDao
import com.example.financetracker.data.local.dao.CategoryDao
import com.example.financetracker.data.local.dao.RolloverDao
import com.example.financetracker.data.local.dao.TransactionDao
import com.example.financetracker.data.local.entity.BudgetRolloverEntity
import com.example.financetracker.data.local.entity.CategoryBudgetEntity
import com.example.financetracker.data.local.entity.CategoryEntity
import com.example.financetracker.data.local.entity.MonthlyBudgetEntity
import com.example.financetracker.data.local.entity.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        MonthlyBudgetEntity::class,
        CategoryBudgetEntity::class,
        TransactionEntity::class,
        BudgetRolloverEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun transactionDao(): TransactionDao
    abstract fun rolloverDao(): RolloverDao

    companion object {
        @Volatile
        private var INSTANCE: FinanceDatabase? = null

        fun getDatabase(context: Context): FinanceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinanceDatabase::class.java,
                    "finance_tracker.db"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
