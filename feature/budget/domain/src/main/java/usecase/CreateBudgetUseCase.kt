package usecase

import models.BudgetModel
import repository.BudgetRepository

class CreateBudgetUseCase(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budget: BudgetModel): Long {
        return repository.insertBudget(budget)
    }
}
