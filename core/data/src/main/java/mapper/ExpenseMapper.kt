package mapper

import local.entity.ExpenseEntity
import models.CategoryModel
import models.ExpenseModel
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun ExpenseEntity.toDomain(): ExpenseModel {
    return ExpenseModel(
        id = id,
        amount = amount,
        category = CategoryModel.valueOf(category),
        description = description,
        date = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(dateMillis),
            ZoneId.systemDefault()
        )
    )
}

fun ExpenseModel.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        amount = amount,
        category = category.name,
        description = description,
        dateMillis = date.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
}
