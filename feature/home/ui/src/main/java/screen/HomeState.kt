package screen

import models.ExpenseModel

sealed interface HomeState {
    data object Loading : HomeState
    data class Success(val expenses: List<ExpenseModel>) : HomeState
    data object Empty : HomeState
    data class Error(val message: String) : HomeState
}
