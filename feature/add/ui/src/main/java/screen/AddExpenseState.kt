package screen

import models.CategoryModel
import java.time.LocalDateTime

data class AddExpenseState(
    val amount: String = "",
    val amountError: String? = null,
    val category: CategoryModel = CategoryModel.FOOD,
    val description: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)
