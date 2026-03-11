package repository

import kotlinx.coroutines.flow.Flow
import models.ExpenseModel
import java.time.LocalDateTime

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseModel>>
    suspend fun getExpenseById(id: Long): ExpenseModel?
    suspend fun insertExpense(expense: ExpenseModel)
    suspend fun deleteExpense(id: Long)
    suspend fun updateExpense(expense: ExpenseModel)
    fun getExpensesByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<ExpenseModel>>
    suspend fun getTotalByDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Double
    suspend fun getAllExpensesOnce(): List<ExpenseModel>
}
