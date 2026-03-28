package usecase

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import model.ExpenseStats
import models.CategoryModel
import models.ExpenseModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import repository.ExpenseRepository
import java.time.LocalDateTime

class GetExpenseStatsUseCaseTest {

    private lateinit var repository: ExpenseRepository
    private lateinit var useCase: GetExpenseStatsUseCase

    @Before
    fun setup() {
        repository = mockk()
        useCase = GetExpenseStatsUseCase(repository)
    }

    @Test
    fun `when no expenses exist, returns empty stats`() = runTest {
        // Given
        coEvery { repository.getTotalByDateRange(any(), any()) } returns 0.0
        coEvery { repository.getAllExpensesOnce() } returns emptyList()

        // When
        val result = useCase()

        // Then
        assertEquals(0.0, result.totalThisMonth, 0.001)
        assertEquals(0.0, result.totalLastMonth, 0.001)
        assertEquals(0.0, result.averagePerDay, 0.001)
        assertEquals(0, result.expenseCount)
        assertNull(result.mostUsedCategory)
        assertNull(result.highestExpense)
        assertNull(result.lowestExpense)
    }

    @Test
    fun `when expenses exist this month, calculates total correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val thisMonthExpenses = listOf(
            ExpenseModel(
                id = 1,
                amount = 50.0,
                category = CategoryModel.FOOD,
                description = "Lunch",
                date = now.withDayOfMonth(10)
            ),
            ExpenseModel(
                id = 2,
                amount = 30.0,
                category = CategoryModel.TRANSPORT,
                description = "Bus",
                date = now.withDayOfMonth(15)
            )
        )

        coEvery { repository.getTotalByDateRange(any(), any()) } returns 80.0
        coEvery { repository.getAllExpensesOnce() } returns thisMonthExpenses

        // When
        val result = useCase()

        // Then
        assertEquals(80.0, result.totalThisMonth, 0.001)
        assertEquals(2, result.expenseCount)
    }

    @Test
    fun `calculates average per day correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val thisMonthExpenses = listOf(
            ExpenseModel(
                id = 1,
                amount = 100.0,
                category = CategoryModel.FOOD,
                description = "Day 1",
                date = now.withDayOfMonth(1).withHour(10)
            ),
            ExpenseModel(
                id = 2,
                amount = 50.0,
                category = CategoryModel.FOOD,
                description = "Day 1 again",
                date = now.withDayOfMonth(1).withHour(15)
            ),
            ExpenseModel(
                id = 3,
                amount = 75.0,
                category = CategoryModel.TRANSPORT,
                description = "Day 2",
                date = now.withDayOfMonth(2).withHour(10)
            )
        )

        // Total: 225.0, Days with expenses: 2, Average: 112.5
        coEvery { repository.getTotalByDateRange(any(), any()) } returns 225.0
        coEvery { repository.getAllExpensesOnce() } returns thisMonthExpenses

        // When
        val result = useCase()

        // Then
        assertEquals(225.0 / 2, result.averagePerDay, 0.001) // Total / distinct days
    }

    @Test
    fun `identifies most used category correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val thisMonthExpenses = listOf(
            ExpenseModel(id = 1, amount = 10.0, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 10.0, category = CategoryModel.FOOD, description = "2", date = now),
            ExpenseModel(id = 3, amount = 10.0, category = CategoryModel.FOOD, description = "3", date = now),
            ExpenseModel(id = 4, amount = 10.0, category = CategoryModel.TRANSPORT, description = "4", date = now),
            ExpenseModel(id = 5, amount = 10.0, category = CategoryModel.SHOPPING, description = "5", date = now)
        )

        coEvery { repository.getTotalByDateRange(any(), any()) } returns 50.0
        coEvery { repository.getAllExpensesOnce() } returns thisMonthExpenses

        // When
        val result = useCase()

        // Then
        assertEquals(CategoryModel.FOOD, result.mostUsedCategory)
    }

    @Test
    fun `calculates highest and lowest expense correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val thisMonthExpenses = listOf(
            ExpenseModel(id = 1, amount = 100.0, category = CategoryModel.FOOD, description = "Highest", date = now),
            ExpenseModel(id = 2, amount = 25.0, category = CategoryModel.TRANSPORT, description = "Middle", date = now),
            ExpenseModel(id = 3, amount = 5.0, category = CategoryModel.OTHER, description = "Lowest", date = now),
            ExpenseModel(id = 4, amount = 50.0, category = CategoryModel.SHOPPING, description = "Another", date = now)
        )

        coEvery { repository.getTotalByDateRange(any(), any()) } returns 180.0
        coEvery { repository.getAllExpensesOnce() } returns thisMonthExpenses

        // When
        val result = useCase()

        // Then
        assertEquals(100.0, result.highestExpense)
        assertEquals(5.0, result.lowestExpense)
    }

    @Test
    fun `filters expenses by current month only`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val lastMonth = now.minusMonths(1)

        val allExpenses = listOf(
            // This month
            ExpenseModel(id = 1, amount = 50.0, category = CategoryModel.FOOD, description = "This month", date = now.withDayOfMonth(10)),
            // Last month
            ExpenseModel(id = 2, amount = 100.0, category = CategoryModel.FOOD, description = "Last month", date = lastMonth.withDayOfMonth(15))
        )

        // Repository will return this month's total only
        coEvery { repository.getTotalByDateRange(any(), any()) } answers {
            val start = arg<LocalDateTime>(0)
            val end = arg<LocalDateTime>(1)
            val filtered = allExpenses.filter { it.date >= start && it.date <= end }
            filtered.sumOf { it.amount }
        }
        coEvery { repository.getAllExpensesOnce() } returns allExpenses

        // When
        val result = useCase()

        // Then
        assertEquals(50.0, result.totalThisMonth, 0.001)
        assertEquals(1, result.expenseCount)
        assertEquals(50.0, result.highestExpense)
    }

    @Test
    fun `compares with last month correctly`() = runTest {
        // Given
        val thisMonthTotal = 150.0
        val lastMonthTotal = 200.0

        coEvery { repository.getTotalByDateRange(any(), any()) } answers {
            // First call: this month, Second call: last month
            val start = arg<LocalDateTime>(0)
            if (start.month == LocalDateTime.now().month) {
                thisMonthTotal
            } else {
                lastMonthTotal
            }
        }
        coEvery { repository.getAllExpensesOnce() } returns listOf(
            ExpenseModel(
                id = 1,
                amount = 150.0,
                category = CategoryModel.FOOD,
                description = "Test",
                date = LocalDateTime.now()
            )
        )

        // When
        val result = useCase()

        // Then
        assertEquals(thisMonthTotal, result.totalThisMonth, 0.001)
        assertEquals(lastMonthTotal, result.totalLastMonth, 0.001)
    }

    @Test
    fun `handles single expense correctly`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val singleExpense = listOf(
            ExpenseModel(
                id = 1,
                amount = 42.0,
                category = CategoryModel.ENTERTAINMENT,
                description = "Cinema",
                date = now
            )
        )

        coEvery { repository.getTotalByDateRange(any(), any()) } returns 42.0
        coEvery { repository.getAllExpensesOnce() } returns singleExpense

        // When
        val result = useCase()

        // Then
        assertEquals(42.0, result.totalThisMonth, 0.001)
        assertEquals(1, result.expenseCount)
        assertEquals(42.0, result.averagePerDay, 0.001)
        assertEquals(CategoryModel.ENTERTAINMENT, result.mostUsedCategory)
        assertEquals(42.0, result.highestExpense)
        assertEquals(42.0, result.lowestExpense)
    }

    @Test
    fun `when multiple categories have same count, returns one as most used`() = runTest {
        // Given
        val now = LocalDateTime.now()
        val expenses = listOf(
            ExpenseModel(id = 1, amount = 10.0, category = CategoryModel.FOOD, description = "1", date = now),
            ExpenseModel(id = 2, amount = 10.0, category = CategoryModel.TRANSPORT, description = "2", date = now)
        )

        coEvery { repository.getTotalByDateRange(any(), any()) } returns 20.0
        coEvery { repository.getAllExpensesOnce() } returns expenses

        // When
        val result = useCase()

        // Then
        // Should return one of them (maxByOrNull picks the first one with max value)
        assert(result.mostUsedCategory == CategoryModel.FOOD || result.mostUsedCategory == CategoryModel.TRANSPORT)
    }
}
