package model

import models.CategoryModel

data class ExpenseStats(
    val totalThisMonth: Double,
    val totalLastMonth: Double,
    val averagePerDay: Double,
    val expenseCount: Int,
    val mostUsedCategory: CategoryModel?,
    val highestExpense: Double?,
    val lowestExpense: Double?
)
