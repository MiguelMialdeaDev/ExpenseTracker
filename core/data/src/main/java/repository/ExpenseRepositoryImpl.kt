package repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import local.dao.ExpenseDao
import mapper.toDomain
import mapper.toEntity
import models.ExpenseModel

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

    override suspend fun deleteExpense(expense: ExpenseModel) {
        dao.deleteExpense(expense.toEntity())
    }
}
