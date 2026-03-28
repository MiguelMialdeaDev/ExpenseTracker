package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import usecase.ExportExpensesToCsvUseCase
import usecase.GetExpenseStatsUseCase
import usecase.GetExpensesByCategoryUseCase

class DashboardViewModel(
    private val getExpenseStatsUseCase: GetExpenseStatsUseCase,
    private val getExpensesByCategoryUseCase: GetExpensesByCategoryUseCase,
    private val exportExpensesToCsvUseCase: ExportExpensesToCsvUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardState.Loading
            try {
                val stats = getExpenseStatsUseCase()
                val categoryExpenses = getExpensesByCategoryUseCase()

                if (stats.expenseCount == 0) {
                    _uiState.value = DashboardState.Empty
                } else {
                    _uiState.value = DashboardState.Success(
                        stats = stats,
                        categoryExpenses = categoryExpenses
                    )
                }
            } catch (e: Exception) {
                _uiState.value = DashboardState.Error(
                    e.message ?: "Error loading dashboard"
                )
            }
        }
    }

    fun refresh() {
        loadDashboard()
    }

    suspend fun exportToCsv(): String {
        return exportExpensesToCsvUseCase()
    }
}
