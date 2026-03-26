package repository

import kotlinx.coroutines.flow.Flow
import models.BudgetModel
import models.CategoryModel

interface BudgetRepository {
    fun getAllBudgets(): Flow<List<BudgetModel>>
    suspend fun getBudgetById(budgetId: Long): BudgetModel?
    suspend fun getBudgetByCategory(category: CategoryModel): BudgetModel?
    suspend fun insertBudget(budget: BudgetModel): Long
    suspend fun updateBudget(budget: BudgetModel)
    suspend fun deleteBudget(budget: BudgetModel)
    suspend fun deleteBudgetById(budgetId: Long)
}
