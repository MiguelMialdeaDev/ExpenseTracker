package local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import models.BudgetModel
import models.CategoryModel

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,
    val monthlyLimit: Double,
    val createdAtMillis: Long
)

fun BudgetEntity.toDomain(): BudgetModel {
    return BudgetModel(
        id = id,
        category = CategoryModel.valueOf(category),
        monthlyLimit = monthlyLimit,
        createdAt = java.time.LocalDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(createdAtMillis),
            java.time.ZoneId.systemDefault()
        )
    )
}

fun BudgetModel.toEntity(): BudgetEntity {
    return BudgetEntity(
        id = id,
        category = category.name,
        monthlyLimit = monthlyLimit,
        createdAtMillis = createdAt.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}
