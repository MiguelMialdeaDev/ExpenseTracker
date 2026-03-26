package screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import models.CategoryModel
import models.ExpenseModel
import usecase.AddExpenseUseCase
import usecase.GetExpenseByIdUseCase
import usecase.UpdateExpenseUseCase
import java.time.LocalDateTime

class AddExpenseViewModel(
    private val addExpenseUseCase: AddExpenseUseCase,
    private val updateExpenseUseCase: UpdateExpenseUseCase,
    private val getExpenseByIdUseCase: GetExpenseByIdUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseState())
    val uiState: StateFlow<AddExpenseState> = _uiState.asStateFlow()

    private var currentExpenseId: Long = 0

    companion object {
        private const val MAX_AMOUNT = 999999.99
        private const val MAX_DESCRIPTION_LENGTH = 200
    }

    @Suppress("TooGenericExceptionCaught")
    fun loadExpense(id: Long?) {
        if (id == null || id == 0L) return

        currentExpenseId = id
        _uiState.update { it.copy(isEditMode = true, isLoading = true) }

        viewModelScope.launch {
            try {
                val expense = getExpenseByIdUseCase(id)
                if (expense != null) {
                    _uiState.update {
                        it.copy(
                            amount = expense.amount.toString(),
                            category = expense.category,
                            description = expense.description,
                            date = expense.date,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Expense not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error loading expense"
                    )
                }
            }
        }
    }

    fun onAmountChange(amount: String) {
        // Solo permitir números y un punto decimal
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            val amountDouble = amount.toDoubleOrNull()
            val error = when {
                amount.isEmpty() -> null
                amountDouble == null -> "Invalid amount"
                amountDouble > MAX_AMOUNT -> "Amount cannot exceed $${MAX_AMOUNT}"
                else -> null
            }

            _uiState.update {
                it.copy(
                    amount = amount,
                    amountError = error
                )
            }
        }
    }

    fun onCategoryChange(category: CategoryModel) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDescriptionChange(description: String) {
        // Limit description length
        if (description.length <= MAX_DESCRIPTION_LENGTH) {
            _uiState.update {
                it.copy(
                    description = description,
                    descriptionError = null
                )
            }
        }
    }

    fun onDateChange(date: LocalDateTime) {
        _uiState.update { it.copy(date = date) }
    }

    @Suppress("TooGenericExceptionCaught")
    fun onSaveClick() {
        if (!validateForm()) return

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            try {
                val amount = _uiState.value.amount.toDouble()
                val expense = ExpenseModel(
                    id = if (_uiState.value.isEditMode) currentExpenseId else 0,
                    amount = amount,
                    category = _uiState.value.category,
                    description = _uiState.value.description,
                    date = _uiState.value.date
                )

                if (_uiState.value.isEditMode) {
                    updateExpenseUseCase(expense)
                } else {
                    addExpenseUseCase(expense)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Error saving expense"
                    )
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        val amount = _uiState.value.amount
        val description = _uiState.value.description.trim()
        var isValid = true

        // Validar amount
        when {
            amount.isEmpty() -> {
                _uiState.update { it.copy(amountError = "Amount is required") }
                isValid = false
            }
            amount.toDoubleOrNull() == null -> {
                _uiState.update { it.copy(amountError = "Invalid amount") }
                isValid = false
            }
            amount.toDouble() <= 0 -> {
                _uiState.update { it.copy(amountError = "Amount must be greater than 0") }
                isValid = false
            }
            amount.toDouble() > MAX_AMOUNT -> {
                _uiState.update { it.copy(amountError = "Amount cannot exceed $${MAX_AMOUNT}") }
                isValid = false
            }
        }

        // Validar description
        if (description.isEmpty()) {
            // Auto-fill with default description instead of showing error
            _uiState.update { it.copy(description = "No description") }
        }

        return isValid
    }
}
