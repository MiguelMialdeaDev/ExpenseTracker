package usecase

import models.ExpenseModel
import repository.ExpenseRepository

class GetExpenseByIdUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(id: Long): ExpenseModel? {
        return repository.getExpenseById(id)
    }
}
