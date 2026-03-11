package usecase

import model.CategoryExpense
import repository.ExpenseRepository
import java.time.LocalDateTime

class GetExpensesByCategoryUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): List<CategoryExpense> {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
            .withHour(23).withMinute(59).withSecond(59)

        val expenses = repository.getAllExpensesOnce()
            .filter { it.date >= startOfMonth && it.date <= endOfMonth }

        if (expenses.isEmpty()) return emptyList()

        val total = expenses.sumOf { it.amount }

        return expenses
            .groupBy { it.category }
            .map { (category, categoryExpenses) ->
                val categoryTotal = categoryExpenses.sumOf { it.amount }
                CategoryExpense(
                    category = category,
                    total = categoryTotal,
                    count = categoryExpenses.size,
                    percentage = ((categoryTotal / total) * 100).toFloat()
                )
            }
            .sortedByDescending { it.total }
    }
}
