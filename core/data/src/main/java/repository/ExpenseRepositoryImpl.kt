package repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import local.dao.ExpenseDao
import mapper.toDomain
import mapper.toEntity
import models.ExpenseModel
import java.time.LocalDateTime
import java.time.ZoneId

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun getAllExpenses(): Flow<List<ExpenseModel>> {
        return dao.getAllExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getExpenseById(id: Long): ExpenseModel? {
        return dao.getExpenseById(id)?.toDomain()
    }

    override suspend fun insertExpense(expense: ExpenseModel) {
        dao.insertExpense(expense.toEntity())
    }

    override suspend fun updateExpense(expense: ExpenseModel) {
        dao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(id: Long) {
        dao.deleteExpense(id)
    }

    override fun getExpensesByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<ExpenseModel>> {
        val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dao.getExpensesByDateRange(startMillis, endMillis).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getTotalByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Double {
        val startMillis = startDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMillis = endDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return dao.getTotalByDateRange(startMillis, endMillis) ?: 0.0
    }

    override suspend fun getAllExpensesOnce(): List<ExpenseModel> {
        return dao.getAllExpensesOnce().map { it.toDomain() }
    }
}
