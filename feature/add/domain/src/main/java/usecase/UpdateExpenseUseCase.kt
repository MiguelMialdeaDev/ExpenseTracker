package usecase

import models.ExpenseModel
import repository.ExpenseRepository

class UpdateExpenseUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(expense: ExpenseModel) {
        repository.updateExpense(expense)
    }
}
