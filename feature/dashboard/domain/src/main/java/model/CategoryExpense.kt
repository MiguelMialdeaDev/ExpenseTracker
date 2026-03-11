package model

import models.CategoryModel

data class CategoryExpense(
    val category: CategoryModel,
    val total: Double,
    val count: Int,
    val percentage: Float
)
