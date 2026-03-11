package usecase

import model.ExpenseStats
import repository.ExpenseRepository
import java.time.LocalDateTime

private const val FIRST_DAY_OF_MONTH = 1
private const val ONE_MONTH = 1L
private const val ONE_DAY = 1L
private const val END_HOUR = 23
private const val END_MINUTE = 59
private const val END_SECOND = 59

class GetExpenseStatsUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): ExpenseStats {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(FIRST_DAY_OF_MONTH).withHour(0).withMinute(0).withSecond(0)
        val endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
            .withHour(END_HOUR).withMinute(END_MINUTE).withSecond(END_SECOND)

        val lastMonthStart = startOfMonth.minusMonths(ONE_MONTH)
        val lastMonthEnd = startOfMonth.minusDays(
            ONE_DAY
        ).withHour(END_HOUR).withMinute(END_MINUTE).withSecond(END_SECOND)

        val totalThisMonth = repository.getTotalByDateRange(startOfMonth, endOfMonth)
        val totalLastMonth = repository.getTotalByDateRange(lastMonthStart, lastMonthEnd)

        val allExpenses = repository.getAllExpensesOnce()
        val thisMonthExpenses = allExpenses.filter {
            it.date >= startOfMonth && it.date <= endOfMonth
        }

        val averagePerDay = if (thisMonthExpenses.isNotEmpty()) {
            val daysWithExpenses = thisMonthExpenses.map { it.date.toLocalDate() }.distinct().size
            if (daysWithExpenses > 0) totalThisMonth / daysWithExpenses else 0.0
        } else {
            0.0
        }

        val mostUsedCategory = thisMonthExpenses
            .groupBy { it.category }
            .maxByOrNull { it.value.size }
            ?.key

        return ExpenseStats(
            totalThisMonth = totalThisMonth,
            totalLastMonth = totalLastMonth,
            averagePerDay = averagePerDay,
            expenseCount = thisMonthExpenses.size,
            mostUsedCategory = mostUsedCategory
        )
    }
}
