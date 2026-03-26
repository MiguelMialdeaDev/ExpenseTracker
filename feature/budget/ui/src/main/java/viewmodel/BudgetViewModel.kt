package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import models.BudgetModel
import usecase.CreateBudgetUseCase
import usecase.DeleteBudgetUseCase
import usecase.GetAllBudgetStatusesUseCase
import usecase.UpdateBudgetUseCase

class BudgetViewModel(
    private val getAllBudgetStatusesUseCase: GetAllBudgetStatusesUseCase,
    private val createBudgetUseCase: CreateBudgetUseCase,
    private val updateBudgetUseCase: UpdateBudgetUseCase,
    private val deleteBudgetUseCase: DeleteBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BudgetState>(BudgetState.Loading)
    val uiState: StateFlow<BudgetState> = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadBudgets() {
        viewModelScope.launch {
            _uiState.value = BudgetState.Loading
            try {
                val statuses = getAllBudgetStatusesUseCase()
                _uiState.value = if (statuses.isEmpty()) {
                    BudgetState.Empty
                } else {
                    BudgetState.Success(statuses)
                }
            } catch (e: Exception) {
                _uiState.value = BudgetState.Error(
                    e.message ?: "Error loading budgets"
                )
            }
        }
    }

    fun createBudget(budget: BudgetModel) {
        viewModelScope.launch {
            try {
                createBudgetUseCase(budget)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.value = BudgetState.Error(
                    e.message ?: "Error creating budget"
                )
            }
        }
    }

    fun updateBudget(budget: BudgetModel) {
        viewModelScope.launch {
            try {
                updateBudgetUseCase(budget)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.value = BudgetState.Error(
                    e.message ?: "Error updating budget"
                )
            }
        }
    }

    fun deleteBudget(budgetId: Long) {
        viewModelScope.launch {
            try {
                deleteBudgetUseCase(budgetId)
                loadBudgets()
            } catch (e: Exception) {
                _uiState.value = BudgetState.Error(
                    e.message ?: "Error deleting budget"
                )
            }
        }
    }
}
