package usecase

import model.CategoryExpense
import repository.ExpenseRepository
import java.time.LocalDateTime

private const val FIRST_DAY_OF_MONTH = 1
private const val END_HOUR = 23
private const val END_MINUTE = 59
private const val END_SECOND = 59
private const val PERCENTAGE_MULTIPLIER = 100

class GetExpensesByCategoryUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): List<CategoryExpense> {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(FIRST_DAY_OF_MONTH).withHour(0).withMinute(0).withSecond(0)
        val endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
            .withHour(END_HOUR).withMinute(END_MINUTE).withSecond(END_SECOND)

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
                    percentage = ((categoryTotal / total) * PERCENTAGE_MULTIPLIER).toFloat()
                )
            }
            .sortedByDescending { it.total }
    }
}
