package repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import local.dao.BudgetDao
import local.entity.toEntity
import local.entity.toDomain
import models.BudgetModel
import models.CategoryModel

class BudgetRepositoryImpl(
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun getAllBudgets(): Flow<List<BudgetModel>> {
        return budgetDao.getAllBudgets().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getBudgetById(budgetId: Long): BudgetModel? {
        return budgetDao.getBudgetById(budgetId)?.toDomain()
    }

    override suspend fun getBudgetByCategory(category: CategoryModel): BudgetModel? {
        return budgetDao.getBudgetByCategory(category.name)?.toDomain()
    }

    override suspend fun insertBudget(budget: BudgetModel): Long {
        return budgetDao.insertBudget(budget.toEntity())
    }

    override suspend fun updateBudget(budget: BudgetModel) {
        budgetDao.updateBudget(budget.toEntity())
    }

    override suspend fun deleteBudget(budget: BudgetModel) {
        budgetDao.deleteBudget(budget.toEntity())
    }

    override suspend fun deleteBudgetById(budgetId: Long) {
        budgetDao.deleteBudgetById(budgetId)
    }
}
