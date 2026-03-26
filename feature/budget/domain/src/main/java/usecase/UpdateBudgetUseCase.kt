package usecase

import models.BudgetModel
import repository.BudgetRepository

class UpdateBudgetUseCase(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: BudgetModel) {
        repository.updateBudget(budget)
    }
}
