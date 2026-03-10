package repository

import kotlinx.coroutines.flow.Flow
import models.ExpenseModel

interface ExpenseRepository {
    fun getAllExpenses(): Flow<List<ExpenseModel>>
    suspend fun getExpenseById(id: Long): ExpenseModel?
    suspend fun insertExpense(expense: ExpenseModel)
    suspend fun deleteExpense(id: Long)
    suspend fun updateExpense(expense: ExpenseModel)
}
