package usecase

import models.ExpenseModel
import repository.ExpenseRepository

class AddExpenseUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseModel) {
        repository.insertExpense(expense)
    }
}
