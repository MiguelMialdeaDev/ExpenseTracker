package model

import models.BudgetModel

data class BudgetStatus(
    val budget: BudgetModel,
    val weeklyLimit: Double,
    val currentWeekSpent: Double,
    val rolloverFromLastWeek: Double,
    val adjustedWeeklyLimit: Double,
    val percentageUsed: Float,
    val projectedMonthlySpending: Double,
    val status: Status
) {
    enum class Status {
        OK,
        WARNING,
        EXCEEDED
    }
}
