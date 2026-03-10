package usecase

import kotlinx.coroutines.flow.Flow
import models.ExpenseModel
import repository.ExpenseRepository

class GetExpensesUseCase(
    private val repository: ExpenseRepository
) {
    operator fun invoke(): Flow<List<ExpenseModel>> {
        return repository.getAllExpenses()
    }
}
