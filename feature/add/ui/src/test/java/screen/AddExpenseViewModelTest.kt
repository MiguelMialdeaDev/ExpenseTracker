package screen

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
import models.CategoryModel
import models.ExpenseModel
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import usecase.AddExpenseUseCase
import usecase.GetExpenseByIdUseCase
import usecase.UpdateExpenseUseCase
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AddExpenseViewModelTest {

    private lateinit var addExpenseUseCase: AddExpenseUseCase
    private lateinit var updateExpenseUseCase: UpdateExpenseUseCase
    private lateinit var getExpenseByIdUseCase: GetExpenseByIdUseCase
    private lateinit var viewModel: AddExpenseViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        addExpenseUseCase = mockk()
        updateExpenseUseCase = mockk()
        getExpenseByIdUseCase = mockk()

        viewModel = AddExpenseViewModel(
            addExpenseUseCase,
            updateExpenseUseCase,
            getExpenseByIdUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value

        assertEquals("", state.amount)
        assertNull(state.amountError)
        assertEquals(CategoryModel.FOOD, state.category)
        assertEquals("", state.description)
        assertNull(state.descriptionError)
        assertFalse(state.isEditMode)
        assertFalse(state.isLoading)
        assertFalse(state.isSaved)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadExpense with null id does not load`() = runTest {
        viewModel.loadExpense(null)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isEditMode)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { getExpenseByIdUseCase(any()) }
    }

    @Test
    fun `loadExpense with 0 id does not load`() = runTest {
        viewModel.loadExpense(0L)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isEditMode)
        assertFalse(state.isLoading)
        coVerify(exactly = 0) { getExpenseByIdUseCase(any()) }
    }

    @Test
    fun `loadExpense successfully loads expense data`() = runTest {
        // Given
        val expenseId = 123L
        val existingExpense = ExpenseModel(
            id = expenseId,
            amount = 50.75,
            category = CategoryModel.TRANSPORT,
            description = "Bus fare",
            date = LocalDateTime.of(2026, 3, 26, 10, 30)
        )
        coEvery { getExpenseByIdUseCase(expenseId) } returns existingExpense

        // When
        viewModel.loadExpense(expenseId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertFalse(state.isLoading)
        assertEquals("50.75", state.amount)
        assertEquals(CategoryModel.TRANSPORT, state.category)
        assertEquals("Bus fare", state.description)
        assertEquals(existingExpense.date, state.date)
        assertNull(state.errorMessage)
    }

    @Test
    fun `loadExpense when expense not found shows error`() = runTest {
        // Given
        coEvery { getExpenseByIdUseCase(999L) } returns null

        // When
        viewModel.loadExpense(999L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isEditMode)
        assertFalse(state.isLoading)
        assertEquals("Expense not found", state.errorMessage)
    }

    @Test
    fun `loadExpense when exception thrown shows error`() = runTest {
        // Given
        val errorMessage = "Database error"
        coEvery { getExpenseByIdUseCase(any()) } throws RuntimeException(errorMessage)

        // When
        viewModel.loadExpense(1L)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.errorMessage)
    }

    @Test
    fun `onAmountChange updates amount for valid input`() {
        viewModel.onAmountChange("123.45")

        val state = viewModel.uiState.value
        assertEquals("123.45", state.amount)
        assertNull(state.amountError)
    }

    @Test
    fun `onAmountChange allows empty string`() {
        viewModel.onAmountChange("")

        val state = viewModel.uiState.value
        assertEquals("", state.amount)
        assertNull(state.amountError)
    }

    @Test
    fun `onAmountChange allows integers`() {
        viewModel.onAmountChange("100")

        val state = viewModel.uiState.value
        assertEquals("100", state.amount)
        assertNull(state.amountError)
    }

    @Test
    fun `onAmountChange allows one decimal point`() {
        viewModel.onAmountChange("50.")

        val state = viewModel.uiState.value
        assertEquals("50.", state.amount)
        assertNull(state.amountError)
    }

    @Test
    fun `onAmountChange allows up to two decimal places`() {
        viewModel.onAmountChange("99.99")

        val state = viewModel.uiState.value
        assertEquals("99.99", state.amount)
        assertNull(state.amountError)
    }

    @Test
    fun `onAmountChange rejects more than two decimal places`() {
        viewModel.onAmountChange("10.123")

        val state = viewModel.uiState.value
        assertEquals("", state.amount) // Should keep previous valid value (empty in this case)
    }

    @Test
    fun `onAmountChange rejects non-numeric input`() {
        viewModel.onAmountChange("abc")

        val state = viewModel.uiState.value
        assertEquals("", state.amount)
    }

    @Test
    fun `onAmountChange shows error when amount exceeds maximum`() {
        viewModel.onAmountChange("1000000.00")

        val state = viewModel.uiState.value
        assertEquals("1000000.00", state.amount)
        assertEquals("Amount cannot exceed $999999.99", state.amountError)
    }

    @Test
    fun `onAmountChange accepts amount at maximum`() {
        viewModel.onAmountChange("999999.99")

        val state = viewModel.uiState.value
        assertEquals("999999.99", state.amount)
        assertNull(state.amountError)
    }

    @Test
    fun `onCategoryChange updates category`() {
        viewModel.onCategoryChange(CategoryModel.SHOPPING)

        val state = viewModel.uiState.value
        assertEquals(CategoryModel.SHOPPING, state.category)
    }

    @Test
    fun `onDescriptionChange updates description`() {
        viewModel.onDescriptionChange("Grocery shopping")

        val state = viewModel.uiState.value
        assertEquals("Grocery shopping", state.description)
        assertNull(state.descriptionError)
    }

    @Test
    fun `onDescriptionChange allows up to 200 characters`() {
        val maxDescription = "a".repeat(200)
        viewModel.onDescriptionChange(maxDescription)

        val state = viewModel.uiState.value
        assertEquals(maxDescription, state.description)
        assertNull(state.descriptionError)
    }

    @Test
    fun `onDescriptionChange rejects more than 200 characters`() {
        val tooLongDescription = "a".repeat(201)
        viewModel.onDescriptionChange(tooLongDescription)

        val state = viewModel.uiState.value
        assertEquals("", state.description) // Should keep previous value (empty in this case)
    }

    @Test
    fun `onDateChange updates date`() {
        val newDate = LocalDateTime.of(2026, 6, 15, 14, 30)
        viewModel.onDateChange(newDate)

        val state = viewModel.uiState.value
        assertEquals(newDate, state.date)
    }

    @Test
    fun `onSaveClick in add mode successfully saves expense`() = runTest {
        // Given
        viewModel.onAmountChange("50.00")
        viewModel.onCategoryChange(CategoryModel.FOOD)
        viewModel.onDescriptionChange("Lunch")

        coEvery { addExpenseUseCase(any()) } returns Unit

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isSaved)
        assertNull(state.errorMessage)
        coVerify { addExpenseUseCase(any()) }
        coVerify(exactly = 0) { updateExpenseUseCase(any()) }
    }

    @Test
    fun `onSaveClick in edit mode successfully updates expense`() = runTest {
        // Given
        val expenseId = 100L
        val existingExpense = ExpenseModel(
            id = expenseId,
            amount = 30.0,
            category = CategoryModel.TRANSPORT,
            description = "Old description",
            date = LocalDateTime.now()
        )
        coEvery { getExpenseByIdUseCase(expenseId) } returns existingExpense
        coEvery { updateExpenseUseCase(any()) } returns Unit

        viewModel.loadExpense(expenseId)
        testDispatcher.scheduler.advanceUntilIdle()

        // Modify the expense
        viewModel.onAmountChange("45.00")
        viewModel.onDescriptionChange("Updated description")

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertFalse(state.isLoading)
        coVerify { updateExpenseUseCase(any()) }
        coVerify(exactly = 0) { addExpenseUseCase(any()) }
    }

    @Test
    fun `onSaveClick fails when amount is empty`() = runTest {
        // Given - amount is empty
        viewModel.onDescriptionChange("Test")

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertEquals("Amount is required", state.amountError)
        coVerify(exactly = 0) { addExpenseUseCase(any()) }
    }

    @Test
    fun `onSaveClick fails when amount is zero`() = runTest {
        // Given
        viewModel.onAmountChange("0")

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertEquals("Amount must be greater than 0", state.amountError)
    }

    @Test
    fun `onSaveClick fails when amount exceeds maximum`() = runTest {
        // Given
        viewModel.onAmountChange("1000000")

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertEquals("Amount cannot exceed $999999.99", state.amountError)
    }

    @Test
    fun `onSaveClick auto-fills description when empty`() = runTest {
        // Given
        viewModel.onAmountChange("100.00")
        coEvery { addExpenseUseCase(any()) } returns Unit

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("No description", state.description)
        assertTrue(state.isSaved)
    }

    @Test
    fun `onSaveClick handles exception during save`() = runTest {
        // Given
        viewModel.onAmountChange("50.00")
        val errorMessage = "Network error"
        coEvery { addExpenseUseCase(any()) } throws RuntimeException(errorMessage)

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertFalse(state.isSaved)
        assertFalse(state.isLoading)
        assertEquals(errorMessage, state.errorMessage)
    }

    @Test
    fun `onSaveClick shows generic error when exception message is null`() = runTest {
        // Given
        viewModel.onAmountChange("50.00")
        coEvery { addExpenseUseCase(any()) } throws RuntimeException(null as String?)

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertEquals("Error saving expense", state.errorMessage)
    }

    @Test
    fun `onSaveClick completes successfully and clears loading state`() = runTest {
        // Given
        viewModel.onAmountChange("50.00")
        coEvery { addExpenseUseCase(any()) } returns Unit

        // When
        viewModel.onSaveClick()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Verify final state after save completes
        val state = viewModel.uiState.value
        assertFalse(state.isLoading) // Loading should be done
        assertTrue(state.isSaved)     // Save should be successful
        assertNull(state.errorMessage)
    }
}
