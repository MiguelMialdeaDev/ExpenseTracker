package usecase

import repository.BudgetRepository

class DeleteBudgetUseCase(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(budgetId: Long) {
        repository.deleteBudgetById(budgetId)
    }
}
