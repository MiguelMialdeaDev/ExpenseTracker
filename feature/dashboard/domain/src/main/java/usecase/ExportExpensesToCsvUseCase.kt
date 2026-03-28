package usecase

import repository.ExpenseRepository
import java.time.format.DateTimeFormatter
import java.util.Locale

class ExportExpensesToCsvUseCase(
    private val repository: ExpenseRepository
) {
    suspend operator fun invoke(): String {
        val expenses = repository.getAllExpensesOnce()

        if (expenses.isEmpty()) {
            return ""
        }

        val header = "Date,Category,Description,Amount\n"
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

        val rows = expenses.joinToString("\n") { expense ->
            val date = expense.date.format(dateFormatter)
            val category = expense.category.name
            val description = expense.description.replace(",", ";") // Escape commas
            val amount = String.format(Locale.US, "%.2f", expense.amount)
            "$date,$category,$description,$amount"
        }

        return header + rows
    }
}
