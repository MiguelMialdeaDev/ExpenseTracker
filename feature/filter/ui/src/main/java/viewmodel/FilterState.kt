package viewmodel

import models.ExpenseModel

sealed interface FilterState {
    data object Loading : FilterState
    data class Success(val expenses: List<ExpenseModel>) : FilterState
    data object Empty : FilterState
}
