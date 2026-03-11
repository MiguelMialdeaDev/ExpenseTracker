package model

import models.CategoryModel
import java.time.LocalDateTime

data class FilterCriteria(
    val category: CategoryModel? = null,
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val minAmount: Double? = null,
    val maxAmount: Double? = null
)
