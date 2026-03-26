package usecase

import kotlinx.coroutines.flow.first
import model.BudgetStatus
import repository.BudgetRepository

class GetAllBudgetStatusesUseCase(
    private val budgetRepository: BudgetRepository,
    private val getBudgetStatusUseCase: GetBudgetStatusUseCase
) {
    suspend operator fun invoke(): List<BudgetStatus> {
        val budgets = budgetRepository.getAllBudgets().first()
        return budgets.mapNotNull { budget ->
            getBudgetStatusUseCase(budget.category)
        }
    }
}
