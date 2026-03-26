package viewmodel

import model.BudgetStatus

sealed interface BudgetState {
    data object Loading : BudgetState
    data class Success(val budgetStatuses: List<BudgetStatus>) : BudgetState
    data object Empty : BudgetState
    data class Error(val message: String) : BudgetState
}
