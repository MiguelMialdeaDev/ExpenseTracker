package local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import local.dao.ExpenseDao
import local.entity.ExpenseEntity

@Database(
    entities = [ExpenseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
