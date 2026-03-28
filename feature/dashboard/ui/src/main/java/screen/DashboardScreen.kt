package screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import kotlinx.coroutines.launch
import model.CategoryExpense
import model.ExpenseStats
import models.CategoryModel
import org.koin.androidx.compose.koinViewModel
import viewmodel.DashboardState
import viewmodel.DashboardViewModel
import java.text.NumberFormat
import java.util.Locale

private const val PERCENTAGE_MULTIPLIER = 100
private const val GREEN_COLOR = 0xFF4CAF50

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val csvContent = viewModel.exportToCsv()
                                    if (csvContent.isEmpty()) {
                                        snackbarHostState.showSnackbar("No expenses to export")
                                    } else {
                                        // TODO: Share CSV file using Android's share intent
                                        // For now, just show success message
                                        snackbarHostState.showSnackbar("CSV generated (${csvContent.lines().size - 1} expenses)")
                                    }
                                } catch (e: Exception) {
                                    snackbarHostState.showSnackbar("Error exporting: ${e.message}")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export to CSV"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is DashboardState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DashboardState.Success -> {
                    DashboardContent(
                        stats = state.stats,
                        categoryExpenses = state.categoryExpenses
                    )
                }

                is DashboardState.Empty -> {
                    EmptyDashboard(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DashboardState.Error -> {
                    ErrorDashboard(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardContent(
    stats: ExpenseStats,
    categoryExpenses: List<CategoryExpense>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stats Cards
        item {
            StatsCard(stats = stats)
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickStatCard(
                    label = "Highest",
                    value = formatCurrency(stats.highestExpense ?: 0.0),
                    emoji = "📈",
                    modifier = Modifier.weight(1f)
                )
                QuickStatCard(
                    label = "Lowest",
                    value = formatCurrency(stats.lowestExpense ?: 0.0),
                    emoji = "📉",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Category Chart
        if (categoryExpenses.isNotEmpty()) {
            item {
                CategoryChart(categoryExpenses = categoryExpenses)
            }
        }

        // Category Breakdown Title
        item {
            Text(
                text = "Expenses by Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Category List
        items(categoryExpenses) { categoryExpense ->
            CategoryExpenseCard(categoryExpense = categoryExpense)
        }
    }
}

@Composable
private fun StatsCard(stats: ExpenseStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "This Month",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Total This Month
            StatItem(
                label = "Total Spent",
                value = formatCurrency(stats.totalThisMonth),
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Average Per Day
                StatItem(
                    label = "Avg/Day",
                    value = formatCurrency(stats.averagePerDay),
                    modifier = Modifier.weight(1f)
                )

                // Expense Count
                StatItem(
                    label = "Transactions",
                    value = stats.expenseCount.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Comparison with last month
            if (stats.totalLastMonth > 0) {
                val difference = stats.totalThisMonth - stats.totalLastMonth
                val percentageChange = ((difference / stats.totalLastMonth) * PERCENTAGE_MULTIPLIER).toInt()
                val isIncrease = difference > 0

                Text(
                    text = if (isIncrease) {
                        "↑ ${formatCurrency(difference)} ($percentageChange%) vs last month"
                    } else {
                        "↓ ${formatCurrency(-difference)} ($percentageChange%) vs last month"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isIncrease) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color(GREEN_COLOR)
                    }
                )
            }

            // Most Used Category
            stats.mostUsedCategory?.let { category ->
                Text(
                    text = "Most used: ${category.emoji} ${category.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun QuickStatCard(
    label: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryChart(categoryExpenses: List<CategoryExpense>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Spending Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (categoryExpenses.isNotEmpty()) {
                val modelProducer = remember { CartesianChartModelProducer.build() }

                // Prepare data for chart
                val chartData = categoryExpenses.map { it.total.toFloat() }
                modelProducer.tryRunTransaction {
                    columnSeries {
                        series(chartData)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp)
                ) {
                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberColumnCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        ),
                        modelProducer = modelProducer
                    )
                }

                // Legend
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryExpenses.forEach { categoryExpense ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = categoryExpense.category.color,
                                            shape = CircleShape
                                        )
                                )
                                Text(
                                    text = "${categoryExpense.category.emoji} ${categoryExpense.category.name}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = formatCurrency(categoryExpense.total),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryExpenseCard(categoryExpense: CategoryExpense) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(categoryExpense.category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = categoryExpense.category.emoji,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            // Category Name and Count
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = categoryExpense.category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${categoryExpense.count} transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Total and Percentage
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = formatCurrency(categoryExpense.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = categoryExpense.category.color
                )
                Text(
                    text = "${categoryExpense.percentage.toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyDashboard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "📊",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "No data yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Add some expenses to see your dashboard",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorDashboard(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "Error loading dashboard",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
    return formatter.format(amount)
}

// Extension properties
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
