package usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import model.FilterCriteria
import models.ExpenseModel
import repository.ExpenseRepository

class GetFilteredExpensesUseCase(
    private val repository: ExpenseRepository
) {
    operator fun invoke(criteria: FilterCriteria): Flow<List<ExpenseModel>> {
        return repository.getAllExpenses().map { expenses ->
            expenses.filter { expense -> matchesCriteria(expense, criteria) }
        }
    }

    private fun matchesCriteria(expense: ExpenseModel, criteria: FilterCriteria): Boolean {
        return matchesCategory(expense, criteria.category) &&
            matchesDateRange(expense, criteria.startDate, criteria.endDate) &&
            matchesAmountRange(expense, criteria.minAmount, criteria.maxAmount)
    }

    private fun matchesCategory(expense: ExpenseModel, category: models.CategoryModel?): Boolean {
        return category?.let { expense.category == it } ?: true
    }

    private fun matchesDateRange(
        expense: ExpenseModel,
        startDate: java.time.LocalDateTime?,
        endDate: java.time.LocalDateTime?
    ): Boolean {
        val matchesStart = startDate?.let { expense.date >= it } ?: true
        val matchesEnd = endDate?.let { expense.date <= it } ?: true
        return matchesStart && matchesEnd
    }

    private fun matchesAmountRange(
        expense: ExpenseModel,
        minAmount: Double?,
        maxAmount: Double?
    ): Boolean {
        val matchesMin = minAmount?.let { expense.amount >= it } ?: true
        val matchesMax = maxAmount?.let { expense.amount <= it } ?: true
        return matchesMin && matchesMax
    }
}
