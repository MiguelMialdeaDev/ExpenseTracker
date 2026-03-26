package usecase

import kotlinx.coroutines.flow.Flow
import models.BudgetModel
import repository.BudgetRepository

class GetAllBudgetsUseCase(
    private val repository: BudgetRepository
) {
    operator fun invoke(): Flow<List<BudgetModel>> {
        return repository.getAllBudgets()
    }
}
