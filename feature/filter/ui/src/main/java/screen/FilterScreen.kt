package screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import models.CategoryModel
import models.ExpenseModel
import org.koin.androidx.compose.koinViewModel
import viewmodel.FilterState
import viewmodel.FilterViewModel
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val FIRST_ROW_CATEGORIES = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    onExpenseClick: (Long) -> Unit,
    viewModel: FilterViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter Expenses") },
                actions = {
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Clear All")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CategoryFilter(
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            when (val state = uiState) {
                is FilterState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is FilterState.Success -> {
                    ExpensesList(
                        expenses = state.expenses,
                        onExpenseClick = onExpenseClick
                    )
                }

                is FilterState.Empty -> {
                    EmptyState()
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    selectedCategory: CategoryModel?,
    onCategorySelected: (CategoryModel?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryModel.entries.take(FIRST_ROW_CATEGORIES).forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(if (selectedCategory == category) null else category)
                    },
                    label = { Text("${category.emoji} ${category.name}") }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryModel.entries.drop(FIRST_ROW_CATEGORIES).forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = {
                        onCategorySelected(if (selectedCategory == category) null else category)
                    },
                    label = { Text("${category.emoji} ${category.name}") }
                )
            }
        }
    }
}

@Composable
private fun ExpensesList(
    expenses: List<ExpenseModel>,
    onExpenseClick: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "${expenses.size} expenses found",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(expenses) { expense ->
            ExpenseCard(
                expense = expense,
                onClick = { onExpenseClick(expense.id) }
            )
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: ExpenseModel,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(expense.category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = expense.category.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = expense.description,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = expense.date.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = formatCurrency(expense.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = expense.category.color
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔍",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "No expenses found",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "Try adjusting your filters",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return formatter.format(amount)
}

private val CategoryModel.emoji: String
    get() = when (this) {
        CategoryModel.FOOD -> "🍔"
        CategoryModel.TRANSPORT -> "🚗"
        CategoryModel.ENTERTAINMENT -> "🎬"
        CategoryModel.SHOPPING -> "🛍️"
        CategoryModel.BILLS -> "📄"
        CategoryModel.HEALTH -> "⚕️"
        CategoryModel.OTHER -> "📌"
    }

private val CategoryModel.color: Color
    get() = when (this) {
        CategoryModel.FOOD -> Color(0xFFFF6B6B)
        CategoryModel.TRANSPORT -> Color(0xFF4ECDC4)
        CategoryModel.ENTERTAINMENT -> Color(0xFFFFE66D)
        CategoryModel.SHOPPING -> Color(0xFFA8E6CF)
        CategoryModel.BILLS -> Color(0xFF95A5A6)
        CategoryModel.HEALTH -> Color(0xFFFF8ED4)
        CategoryModel.OTHER -> Color(0xFFBDC3C7)
    }
