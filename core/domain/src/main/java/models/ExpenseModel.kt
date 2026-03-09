package models

import java.time.LocalDateTime

data class ExpenseModel(
    val id: Long = 0,
    val amount: Double,
    val category: CategoryModel,
    val description: String,
    val date: LocalDateTime
)
