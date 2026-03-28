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

class GetExpensesByCategoryUseCaseTest {

    private lateinit var repository: ExpenseRepository
    private lateinit var useCase: GetExpensesByCategoryUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetExpensesByCategoryUseCase(repository)
    }

    @Test
    fun `when no expenses exist, returns empty list`() = runTest {
        // Given
        coEvery { repository.getAllExpensesOnce() } returns emptyList()

        // When
        val result = useCase()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `when single category exists, calculates correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(
                id = 1,
                amount = 50.0,
                category = CategoryModel.FOOD,
                description = "Lunch",
                date = now
            ),
            ExpenseModel(
                id = 2,
                amount = 30.0,
                category = CategoryModel.FOOD,
                description = "Dinner",
                date = now
            )
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertEquals(1, result.size)
        assertEquals(CategoryModel.FOOD, result[0].category)
        assertEquals(80.0, result[0].total, 0.001)
        assertEquals(2, result[0].count)
        assertEquals(100.0f, result[0].percentage, 0.001f)
    }

    @Test
    fun `when multiple categories exist, groups correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 50.0, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 30.0, category = CategoryModel.FOOD, description = "2", date = now),
            ExpenseModel(id = 3, amount = 20.0, category = CategoryModel.TRANSPORT, description = "3", date = now)
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertEquals(2, result.size)

        val food = result.find { it.category == CategoryModel.FOOD }!!
        assertEquals(80.0, food.total, 0.001)
        assertEquals(2, food.count)

        val transport = result.find { it.category == CategoryModel.TRANSPORT }!!
        assertEquals(20.0, transport.total, 0.001)
        assertEquals(1, transport.count)
    }

    @Test
    fun `calculates percentages correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            // FOOD: 60 out of 100 = 60%
            ExpenseModel(id = 1, amount = 60.0, category = CategoryModel.FOOD, description = "1", date = now),
            // TRANSPORT: 30 out of 100 = 30%
            ExpenseModel(id = 2, amount = 30.0, category = CategoryModel.TRANSPORT, description = "2", date = now),
            // SHOPPING: 10 out of 100 = 10%
            ExpenseModel(id = 3, amount = 10.0, category = CategoryModel.SHOPPING, description = "3", date = now)
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        val food = result.find { it.category == CategoryModel.FOOD }!!
        assertEquals(60.0f, food.percentage, 0.1f)

        val transport = result.find { it.category == CategoryModel.TRANSPORT }!!
        assertEquals(30.0f, transport.percentage, 0.1f)

        val shopping = result.find { it.category == CategoryModel.SHOPPING }!!
        assertEquals(10.0f, shopping.percentage, 0.1f)
    }

    @Test
    fun `sorts categories by total descending`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 10.0, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 50.0, category = CategoryModel.TRANSPORT, description = "2", date = now),
            ExpenseModel(id = 3, amount = 30.0, category = CategoryModel.SHOPPING, description = "3", date = now)
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertEquals(3, result.size)
        assertEquals(CategoryModel.TRANSPORT, result[0].category) // 50
        assertEquals(CategoryModel.SHOPPING, result[1].category)  // 30
        assertEquals(CategoryModel.FOOD, result[2].category)      // 10
    }

    @Test
    fun `filters expenses by current month only`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val lastMonth = now.minusMonths(1)
        val nextMonth = now.plusMonths(1)

        val expenses = listOf(
            // This month
            ExpenseModel(id = 1, amount = 50.0, category = CategoryModel.FOOD, description = "This month", date = now.withDayOfMonth(15)),
            // Last month
            ExpenseModel(id = 2, amount = 100.0, category = CategoryModel.FOOD, description = "Last month", date = lastMonth.withDayOfMonth(15)),
            // Next month
            ExpenseModel(id = 3, amount = 200.0, category = CategoryModel.FOOD, description = "Next month", date = nextMonth.withDayOfMonth(15))
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertEquals(1, result.size)
        assertEquals(50.0, result[0].total, 0.001) // Only this month's expense
    }

    @Test
    fun `counts transactions per category correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 10.0, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 20.0, category = CategoryModel.FOOD, description = "2", date = now),
            ExpenseModel(id = 3, amount = 30.0, category = CategoryModel.FOOD, description = "3", date = now),
            ExpenseModel(id = 4, amount = 40.0, category = CategoryModel.TRANSPORT, description = "4", date = now)
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        val food = result.find { it.category == CategoryModel.FOOD }!!
        assertEquals(3, food.count)

        val transport = result.find { it.category == CategoryModel.TRANSPORT }!!
        assertEquals(1, transport.count)
    }

    @Test
    fun `handles all category types`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 10.0, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 20.0, category = CategoryModel.TRANSPORT, description = "2", date = now),
            ExpenseModel(id = 3, amount = 30.0, category = CategoryModel.ENTERTAINMENT, description = "3", date = now),
            ExpenseModel(id = 4, amount = 40.0, category = CategoryModel.SHOPPING, description = "4", date = now),
            ExpenseModel(id = 5, amount = 50.0, category = CategoryModel.BILLS, description = "5", date = now),
            ExpenseModel(id = 6, amount = 60.0, category = CategoryModel.HEALTH, description = "6", date = now),
            ExpenseModel(id = 7, amount = 70.0, category = CategoryModel.OTHER, description = "7", date = now)
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertEquals(7, result.size)
        val categories = result.map { it.category }.toSet()
        assertTrue(categories.contains(CategoryModel.FOOD))
        assertTrue(categories.contains(CategoryModel.TRANSPORT))
        assertTrue(categories.contains(CategoryModel.ENTERTAINMENT))
        assertTrue(categories.contains(CategoryModel.SHOPPING))
        assertTrue(categories.contains(CategoryModel.BILLS))
        assertTrue(categories.contains(CategoryModel.HEALTH))
        assertTrue(categories.contains(CategoryModel.OTHER))
    }

    @Test
    fun `percentage sums to 100 percent`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 25.0, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 25.0, category = CategoryModel.TRANSPORT, description = "2", date = now),
            ExpenseModel(id = 3, amount = 25.0, category = CategoryModel.SHOPPING, description = "3", date = now),
            ExpenseModel(id = 4, amount = 25.0, category = CategoryModel.OTHER, description = "4", date = now)
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        val totalPercentage = result.sumOf { it.percentage.toDouble() }
        assertEquals(100.0, totalPercentage, 0.1) // Allow small floating point error
    }

    @Test
    fun `handles decimal amounts correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 33.33, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 66.67, category = CategoryModel.TRANSPORT, description = "2", date = now)
        )
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        assertEquals(2, result.size)

        val transport = result[0] // Sorted by total descending
        assertEquals(66.67, transport.total, 0.001)
        assertEquals(66.67f, transport.percentage, 0.1f)

        val food = result[1]
        assertEquals(33.33, food.total, 0.001)
        assertEquals(33.33f, food.percentage, 0.1f)
    }
}
