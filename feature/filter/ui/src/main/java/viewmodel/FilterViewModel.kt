package viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import model.FilterCriteria
import models.CategoryModel
import usecase.GetFilteredExpensesUseCase
import java.time.LocalDateTime

class FilterViewModel(
    private val getFilteredExpensesUseCase: GetFilteredExpensesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<FilterState>(FilterState.Loading)
    val uiState: StateFlow<FilterState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<CategoryModel?>(null)
    val selectedCategory: StateFlow<CategoryModel?> = _selectedCategory.asStateFlow()

    private val _startDate = MutableStateFlow<LocalDateTime?>(null)
    val startDate: StateFlow<LocalDateTime?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<LocalDateTime?>(null)
    val endDate: StateFlow<LocalDateTime?> = _endDate.asStateFlow()

    init {
        applyFilters()
    }

    fun selectCategory(category: CategoryModel?) {
        _selectedCategory.value = category
        applyFilters()
    }

    fun setStartDate(date: LocalDateTime?) {
        _startDate.value = date
        applyFilters()
    }

    fun setEndDate(date: LocalDateTime?) {
        _endDate.value = date
        applyFilters()
    }

    fun clearFilters() {
        _selectedCategory.value = null
        _startDate.value = null
        _endDate.value = null
        applyFilters()
    }

    private fun applyFilters() {
        viewModelScope.launch {
            val criteria = FilterCriteria(
                category = _selectedCategory.value,
                startDate = _startDate.value,
                endDate = _endDate.value
            )

            getFilteredExpensesUseCase(criteria).collectLatest { expenses ->
                _uiState.value = if (expenses.isEmpty()) {
                    FilterState.Empty
                } else {
                    FilterState.Success(expenses)
                }
            }
        }
    }
}
