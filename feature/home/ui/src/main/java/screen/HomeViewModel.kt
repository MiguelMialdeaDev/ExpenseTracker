package screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import usecase.DeleteExpenseUseCase
import usecase.GetExpensesUseCase

class HomeViewModel(
    private val getExpensesUseCase: GetExpensesUseCase,
    private val deleteExpenseUseCase: DeleteExpenseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeState>(HomeState.Loading)
    val uiState: StateFlow<HomeState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private fun loadExpenses() {
        viewModelScope.launch {
            getExpensesUseCase()
                .catch { exception ->
                    _uiState.value = HomeState.Error(
                        exception.message ?: "Unknown error"
                    )
                }
                .collect { expenses ->
                    _uiState.value = if (expenses.isEmpty()) {
                        HomeState.Empty
                    } else {
                        HomeState.Success(expenses)
                    }
                }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    fun onDeleteExpense(id: Long) {
        viewModelScope.launch {
            try {
                deleteExpenseUseCase(id)
                // No necesitamos actualizar manualmente, el Flow lo hace solo
            } catch (e: Exception) {
                _uiState.value = HomeState.Error(
                    e.message ?: "Error deleting expense"
                )
            }
        }
    }

    fun onAddExpenseClick() {
        // Navegación se manejará desde la UI
    }
}
