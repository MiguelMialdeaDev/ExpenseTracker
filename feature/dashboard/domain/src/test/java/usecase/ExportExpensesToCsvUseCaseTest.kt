package usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import models.CategoryModel
import models.ExpenseModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import repository.ExpenseRepository
import java.time.LocalDateTime

class ExportExpensesToCsvUseCaseTest {

    private lateinit var repository: ExpenseRepository
    private lateinit var useCase: ExportExpensesToCsvUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = ExportExpensesToCsvUseCase(repository)
    }

    @Test
    fun `when no expenses exist, returns empty string`() = runTest {
        // Given
        coEvery { repository.getAllExpensesOnce() } returns emptyList()

        // When
        val result = useCase()

        // Then
        assertEquals("", result)
    }

    @Test
    fun `when expenses exist, returns CSV with header`() = runTest {
        // Given
        val expenses = listOf(
            ExpenseModel(
                id = 1,
                amount = 25.50,
                category = CategoryModel.FOOD,
                description = "Lunch",
                date = LocalDateTime.of(2026, 3, 26, 14, 30)
            )
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertTrue(result.startsWith("Date,Category,Description,Amount"))
        assertTrue(result.contains("2026-03-26 14:30,FOOD,Lunch,25.50"))
    }

    @Test
    fun `when expenses exist, formats amount with two decimals`() = runTest {
        // Given
        val expenses = listOf(
            ExpenseModel(
                id = 1,
                amount = 10.5,
                category = CategoryModel.TRANSPORT,
                description = "Bus",
                date = LocalDateTime.of(2026, 3, 25, 9, 15)
            ),
            ExpenseModel(
                id = 2,
                amount = 100.0,
                category = CategoryModel.SHOPPING,
                description = "Clothes",
                date = LocalDateTime.of(2026, 3, 24, 16, 45)
            )
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertTrue(result.contains("10.50"))
        assertTrue(result.contains("100.00"))
    }

    @Test
    fun `when description contains comma, replaces with semicolon`() = runTest {
        // Given
        val expenses = listOf(
            ExpenseModel(
                id = 1,
                amount = 50.0,
                category = CategoryModel.FOOD,
                description = "Lunch, dinner, snacks",
                date = LocalDateTime.of(2026, 3, 26, 14, 30)
            )
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertTrue(result.contains("Lunch; dinner; snacks"))
        // Make sure original commas are not present in description field
        val lines = result.lines()
        val dataLine = lines[1] // Skip header
        val parts = dataLine.split(",")
        assertEquals(4, parts.size) // Should have exactly 4 CSV columns
    }

    @Test
    fun `when multiple expenses exist, exports all in correct order`() = runTest {
        // Given
        val expenses = listOf(
            ExpenseModel(
                id = 1,
                amount = 25.50,
                category = CategoryModel.FOOD,
                description = "Lunch",
                date = LocalDateTime.of(2026, 3, 26, 14, 30)
            ),
            ExpenseModel(
                id = 2,
                amount = 12.00,
                category = CategoryModel.TRANSPORT,
                description = "Uber",
                date = LocalDateTime.of(2026, 3, 25, 9, 15)
            ),
            ExpenseModel(
                id = 3,
                amount = 50.00,
                category = CategoryModel.ENTERTAINMENT,
                description = "Cinema",
                date = LocalDateTime.of(2026, 3, 24, 20, 0)
            )
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        val lines = result.lines()
        assertEquals(4, lines.size) // Header + 3 expenses
        assertEquals("Date,Category,Description,Amount", lines[0])
        assertTrue(lines[1].contains("2026-03-26 14:30,FOOD,Lunch,25.50"))
        assertTrue(lines[2].contains("2026-03-25 09:15,TRANSPORT,Uber,12.00"))
        assertTrue(lines[3].contains("2026-03-24 20:00,ENTERTAINMENT,Cinema,50.00"))
    }

    @Test
    fun `exports all category types correctly`() = runTest {
        // Given
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 10.0, category = CategoryModel.FOOD, description = "Food", date = LocalDateTime.now()),
            ExpenseModel(id = 2, amount = 10.0, category = CategoryModel.TRANSPORT, description = "Transport", date = LocalDateTime.now()),
            ExpenseModel(id = 3, amount = 10.0, category = CategoryModel.ENTERTAINMENT, description = "Entertainment", date = LocalDateTime.now()),
            ExpenseModel(id = 4, amount = 10.0, category = CategoryModel.SHOPPING, description = "Shopping", date = LocalDateTime.now()),
            ExpenseModel(id = 5, amount = 10.0, category = CategoryModel.BILLS, description = "Bills", date = LocalDateTime.now()),
            ExpenseModel(id = 6, amount = 10.0, category = CategoryModel.HEALTH, description = "Health", date = LocalDateTime.now()),
            ExpenseModel(id = 7, amount = 10.0, category = CategoryModel.OTHER, description = "Other", date = LocalDateTime.now())
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertTrue(result.contains("FOOD"))
        assertTrue(result.contains("TRANSPORT"))
        assertTrue(result.contains("ENTERTAINMENT"))
        assertTrue(result.contains("SHOPPING"))
        assertTrue(result.contains("BILLS"))
        assertTrue(result.contains("HEALTH"))
        assertTrue(result.contains("OTHER"))
    }
}
