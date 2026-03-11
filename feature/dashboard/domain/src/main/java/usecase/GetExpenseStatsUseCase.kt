package usecase

import model.ExpenseStats
import repository.ExpenseRepository
import java.time.LocalDateTime

class GetExpenseStatsUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): ExpenseStats {
        val now = LocalDateTime.now()
        val startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val endOfMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
            .withHour(23).withMinute(59).withSecond(59)

        val lastMonthStart = startOfMonth.minusMonths(1)
        val lastMonthEnd = startOfMonth.minusDays(1).withHour(23).withMinute(59).withSecond(59)

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
