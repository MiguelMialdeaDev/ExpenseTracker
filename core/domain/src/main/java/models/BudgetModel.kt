package models

import java.time.LocalDateTime

data class BudgetModel(
    val id: Long = 0,
    val category: CategoryModel,
    val monthlyLimit: Double,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
