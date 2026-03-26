package local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import local.dao.BudgetDao
import local.dao.ExpenseDao
import local.entity.BudgetEntity
import local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class, BudgetEntity::class],
    version = 2,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
}
