package viewmodel

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import model.CategoryExpense
import model.ExpenseStats
import models.CategoryModel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import usecase.ExportExpensesToCsvUseCase
import usecase.GetExpenseStatsUseCase
import usecase.GetExpensesByCategoryUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    private lateinit var getExpenseStatsUseCase: GetExpenseStatsUseCase
    private lateinit var getExpensesByCategoryUseCase: GetExpensesByCategoryUseCase
    private lateinit var exportExpensesToCsvUseCase: ExportExpensesToCsvUseCase
    private lateinit var viewModel: DashboardViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        getExpenseStatsUseCase = mockk()
        getExpensesByCategoryUseCase = mockk()
        exportExpensesToCsvUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading and then loads dashboard on init`() = runTest {
        // Given
        val stats = ExpenseStats(
            totalThisMonth = 100.0,
            totalLastMonth = 80.0,
            averagePerDay = 10.0,
            expenseCount = 5,
            mostUsedCategory = CategoryModel.FOOD,
            highestExpense = 50.0,
            lowestExpense = 5.0
        )
        val categoryExpenses = listOf(
            CategoryExpense(CategoryModel.FOOD, 60.0, 3, 60.0f),
            CategoryExpense(CategoryModel.TRANSPORT, 40.0, 2, 40.0f)
        )

        coEvery { getExpenseStatsUseCase() } returns stats
        coEvery { getExpensesByCategoryUseCase() } returns categoryExpenses

        // When
        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // Then
        viewModel.uiState.test {
            assertEquals(DashboardState.Loading, awaitItem())
            val successState = awaitItem() as DashboardState.Success
            assertEquals(stats, successState.stats)
            assertEquals(categoryExpenses, successState.categoryExpenses)
        }
    }

    @Test
    fun `loadDashboard emits Success state when expenses exist`() = runTest {
        // Given
        val stats = ExpenseStats(
            totalThisMonth = 100.0,
            totalLastMonth = 80.0,
            averagePerDay = 10.0,
            expenseCount = 5,
            mostUsedCategory = CategoryModel.FOOD,
            highestExpense = 50.0,
            lowestExpense = 5.0
        )
        val categoryExpenses = listOf(
            CategoryExpense(CategoryModel.FOOD, 100.0, 5, 100.0f)
        )

        coEvery { getExpenseStatsUseCase() } returns stats
        coEvery { getExpensesByCategoryUseCase() } returns categoryExpenses

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When/Then
        viewModel.uiState.test {
            assertEquals(DashboardState.Loading, awaitItem())

            val successState = awaitItem() as DashboardState.Success
            assertEquals(100.0, successState.stats.totalThisMonth, 0.001)
            assertEquals(5, successState.stats.expenseCount)
            assertEquals(1, successState.categoryExpenses.size)
        }
    }

    @Test
    fun `loadDashboard emits Empty state when no expenses exist`() = runTest {
        // Given
        val emptyStats = ExpenseStats(
            totalThisMonth = 0.0,
            totalLastMonth = 0.0,
            averagePerDay = 0.0,
            expenseCount = 0,
            mostUsedCategory = null,
            highestExpense = null,
            lowestExpense = null
        )

        coEvery { getExpenseStatsUseCase() } returns emptyStats
        coEvery { getExpensesByCategoryUseCase() } returns emptyList()

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When/Then
        viewModel.uiState.test {
            assertEquals(DashboardState.Loading, awaitItem())
            assertEquals(DashboardState.Empty, awaitItem())
        }
    }

    @Test
    fun `loadDashboard emits Error state when use case throws exception`() = runTest {
        // Given
        val errorMessage = "Database connection failed"
        coEvery { getExpenseStatsUseCase() } throws RuntimeException(errorMessage)

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When/Then
        viewModel.uiState.test {
            assertEquals(DashboardState.Loading, awaitItem())

            val errorState = awaitItem() as DashboardState.Error
            assertEquals(errorMessage, errorState.message)
        }
    }

    @Test
    fun `loadDashboard emits generic error message when exception message is null`() = runTest {
        // Given
        coEvery { getExpenseStatsUseCase() } throws RuntimeException(null as String?)

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When/Then
        viewModel.uiState.test {
            assertEquals(DashboardState.Loading, awaitItem())

            val errorState = awaitItem() as DashboardState.Error
            assertEquals("Error loading dashboard", errorState.message)
        }
    }

    @Test
    fun `refresh reloads dashboard data`() = runTest {
        // Given
        val stats = ExpenseStats(
            totalThisMonth = 100.0,
            totalLastMonth = 80.0,
            averagePerDay = 10.0,
            expenseCount = 5,
            mostUsedCategory = CategoryModel.FOOD,
            highestExpense = 50.0,
            lowestExpense = 5.0
        )
        val categoryExpenses = listOf(
            CategoryExpense(CategoryModel.FOOD, 100.0, 5, 100.0f)
        )

        coEvery { getExpenseStatsUseCase() } returns stats
        coEvery { getExpensesByCategoryUseCase() } returns categoryExpenses

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When
        viewModel.uiState.test {
            skipItems(2) // Skip initial Loading and Success states

            viewModel.refresh()

            // Then
            assertEquals(DashboardState.Loading, awaitItem())
            val successState = awaitItem() as DashboardState.Success
            assertEquals(stats, successState.stats)
        }

        // Verify use cases were called twice (once on init, once on refresh)
        coVerify(exactly = 2) { getExpenseStatsUseCase() }
        coVerify(exactly = 2) { getExpensesByCategoryUseCase() }
    }

    @Test
    fun `exportToCsv returns CSV content`() = runTest {
        // Given
        val csvContent = "Date,Category,Description,Amount\n2026-03-26 14:30,FOOD,Lunch,25.50"
        coEvery { exportExpensesToCsvUseCase() } returns csvContent

        // Mock stats and category expenses for initialization
        val stats = ExpenseStats(
            totalThisMonth = 100.0,
            totalLastMonth = 80.0,
            averagePerDay = 10.0,
            expenseCount = 5,
            mostUsedCategory = CategoryModel.FOOD,
            highestExpense = 50.0,
            lowestExpense = 5.0
        )
        coEvery { getExpenseStatsUseCase() } returns stats
        coEvery { getExpensesByCategoryUseCase() } returns emptyList()

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When
        val result = viewModel.exportToCsv()

        // Then
        assertEquals(csvContent, result)
        coVerify { exportExpensesToCsvUseCase() }
    }

    @Test
    fun `exportToCsv returns empty string when no expenses`() = runTest {
        // Given
        coEvery { exportExpensesToCsvUseCase() } returns ""

        // Mock stats and category expenses for initialization
        val stats = ExpenseStats(
            totalThisMonth = 0.0,
            totalLastMonth = 0.0,
            averagePerDay = 0.0,
            expenseCount = 0,
            mostUsedCategory = null,
            highestExpense = null,
            lowestExpense = null
        )
        coEvery { getExpenseStatsUseCase() } returns stats
        coEvery { getExpensesByCategoryUseCase() } returns emptyList()

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When
        val result = viewModel.exportToCsv()

        // Then
        assertTrue(result.isEmpty())
    }

    @Test
    fun `multiple refresh calls emit Loading state each time`() = runTest {
        // Given
        val stats = ExpenseStats(
            totalThisMonth = 100.0,
            totalLastMonth = 80.0,
            averagePerDay = 10.0,
            expenseCount = 5,
            mostUsedCategory = CategoryModel.FOOD,
            highestExpense = 50.0,
            lowestExpense = 5.0
        )

        coEvery { getExpenseStatsUseCase() } returns stats
        coEvery { getExpensesByCategoryUseCase() } returns emptyList()

        viewModel = DashboardViewModel(
            getExpenseStatsUseCase,
            getExpensesByCategoryUseCase,
            exportExpensesToCsvUseCase
        )

        // When/Then
        viewModel.uiState.test {
            skipItems(2) // Skip initial Loading and Success

            // First refresh
            viewModel.refresh()
            assertEquals(DashboardState.Loading, awaitItem())
            awaitItem() // Success

            // Second refresh
            viewModel.refresh()
            assertEquals(DashboardState.Loading, awaitItem())
            awaitItem() // Success
        }
    }
}
