package usecase

import model.BudgetStatus
import models.CategoryModel
import repository.BudgetRepository
import repository.ExpenseRepository
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters

private const val WEEKS_PER_MONTH = 4.33
private const val WARNING_THRESHOLD = 0.8f
private const val EXCEEDED_THRESHOLD = 1.0f
private const val PERCENTAGE_MULTIPLIER = 100

class GetBudgetStatusUseCase(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(category: CategoryModel): BudgetStatus? {
        val budget = budgetRepository.getBudgetByCategory(category) ?: return null

        val now = LocalDateTime.now()
        val weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .withHour(0).withMinute(0).withSecond(0)
        val weekEnd = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
            .withHour(23).withMinute(59).withSecond(59)

        val lastWeekStart = weekStart.minusWeeks(1)
        val lastWeekEnd = weekStart.minusSeconds(1)

        val currentWeekExpenses = expenseRepository.getAllExpensesOnce()
            .filter { it.category == category && it.date >= weekStart && it.date <= weekEnd }

        val lastWeekExpenses = expenseRepository.getAllExpensesOnce()
            .filter { it.category == category && it.date >= lastWeekStart && it.date <= lastWeekEnd }

        val currentWeekSpent = currentWeekExpenses.sumOf { it.amount }
        val lastWeekSpent = lastWeekExpenses.sumOf { it.amount }

        val weeklyLimit = budget.monthlyLimit / WEEKS_PER_MONTH
        val rollover = (weeklyLimit - lastWeekSpent).coerceAtLeast(0.0)
        val adjustedWeeklyLimit = weeklyLimit + rollover

        val percentageUsed = if (adjustedWeeklyLimit > 0) {
            (currentWeekSpent / adjustedWeeklyLimit).toFloat()
        } else {
            0f
        }

        val monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
        val monthExpenses = expenseRepository.getAllExpensesOnce()
            .filter { it.category == category && it.date >= monthStart }

        val daysInMonth = now.toLocalDate().lengthOfMonth()
        val dayOfMonth = now.dayOfMonth
        val avgDailySpending = if (dayOfMonth > 0) {
            monthExpenses.sumOf { it.amount } / dayOfMonth
        } else {
            0.0
        }
        val projectedMonthlySpending = avgDailySpending * daysInMonth

        val status = when {
            percentageUsed >= EXCEEDED_THRESHOLD -> BudgetStatus.Status.EXCEEDED
            percentageUsed >= WARNING_THRESHOLD -> BudgetStatus.Status.WARNING
            else -> BudgetStatus.Status.OK
        }

        return BudgetStatus(
            budget = budget,
            weeklyLimit = weeklyLimit,
            currentWeekSpent = currentWeekSpent,
            rolloverFromLastWeek = rollover,
            adjustedWeeklyLimit = adjustedWeeklyLimit,
            percentageUsed = percentageUsed * PERCENTAGE_MULTIPLIER,
            projectedMonthlySpending = projectedMonthlySpending,
            status = status
        )
    }
}
