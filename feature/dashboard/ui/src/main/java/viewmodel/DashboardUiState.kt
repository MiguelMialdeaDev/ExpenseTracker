package viewmodel

import model.CategoryExpense
import model.ExpenseStats

sealed interface DashboardState {
    data object Loading : DashboardState
    data class Success(
        val stats: ExpenseStats,
        val categoryExpenses: List<CategoryExpense>
    ) : DashboardState
    data object Empty : DashboardState
    data class Error(val message: String) : DashboardState
}
