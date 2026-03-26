package screen

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dialog.AddBudgetDialog
import model.BudgetStatus
import models.CategoryModel
import org.koin.androidx.compose.koinViewModel
import viewmodel.BudgetState
import viewmodel.BudgetViewModel
import java.text.NumberFormat
import java.util.Locale

private const val PERCENTAGE_DIVIDER = 100f

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddBudgetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { budget ->
                viewModel.createBudget(budget)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Plan") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is BudgetState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is BudgetState.Success -> {
                    BudgetContent(
                        budgetStatuses = state.budgetStatuses,
                        onDeleteBudget = { viewModel.deleteBudget(it) }
                    )
                }

                is BudgetState.Empty -> {
                    EmptyBudgetState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is BudgetState.Error -> {
                    ErrorBudgetState(
                        message = state.message,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetContent(
    budgetStatuses: List<BudgetStatus>,
    onDeleteBudget: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(budgetStatuses) { budgetStatus ->
            BudgetStatusCard(
                budgetStatus = budgetStatus,
                onDelete = { onDeleteBudget(budgetStatus.budget.id) }
            )
        }
    }
}

@Composable
private fun BudgetStatusCard(
    budgetStatus: BudgetStatus,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text("Delete budget?")
            },
            text = {
                Text("Are you sure you want to delete the budget for ${budgetStatus.budget.category.name}? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (budgetStatus.status) {
                BudgetStatus.Status.EXCEEDED -> MaterialTheme.colorScheme.errorContainer
                BudgetStatus.Status.WARNING -> Color(0xFFFFF3CD)
                BudgetStatus.Status.OK -> MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(budgetStatus.budget.category.color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = budgetStatus.budget.category.emoji,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }

                    Column {
                        Text(
                            text = budgetStatus.budget.category.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${formatCurrency(budgetStatus.budget.monthlyLimit)}/month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete budget"
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "This week",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${formatCurrency(budgetStatus.currentWeekSpent)} / ${
                            formatCurrency(
                                budgetStatus.adjustedWeeklyLimit
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { (budgetStatus.percentageUsed / PERCENTAGE_DIVIDER).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = when (budgetStatus.status) {
                        BudgetStatus.Status.EXCEEDED -> MaterialTheme.colorScheme.error
                        BudgetStatus.Status.WARNING -> Color(0xFFFFA726)
                        BudgetStatus.Status.OK -> Color(0xFF66BB6A)
                    },
                )

                Text(
                    text = when (budgetStatus.status) {
                        BudgetStatus.Status.EXCEEDED -> "🚨 Over budget by ${
                            formatCurrency(
                                budgetStatus.currentWeekSpent - budgetStatus.adjustedWeeklyLimit
                            )
                        }"

                        BudgetStatus.Status.WARNING -> "⚠️ ${budgetStatus.percentageUsed.toInt()}% used"
                        BudgetStatus.Status.OK -> "✅ On track (${budgetStatus.percentageUsed.toInt()}%)"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when (budgetStatus.status) {
                        BudgetStatus.Status.EXCEEDED -> MaterialTheme.colorScheme.error
                        BudgetStatus.Status.WARNING -> Color(0xFFE65100)
                        BudgetStatus.Status.OK -> Color(0xFF2E7D32)
                    },
                    fontWeight = FontWeight.Medium
                )
            }

            if (budgetStatus.rolloverFromLastWeek > 0) {
                Text(
                    text = "💰 Rollover: +${formatCurrency(budgetStatus.rolloverFromLastWeek)} from last week",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
            }

            Text(
                text = "📊 Projected monthly: ${formatCurrency(budgetStatus.projectedMonthlySpending)} " +
                    if (budgetStatus.projectedMonthlySpending > budgetStatus.budget.monthlyLimit) {
                        "(${formatCurrency(budgetStatus.projectedMonthlySpending - budgetStatus.budget.monthlyLimit)} over)"
                    } else {
                        "(${formatCurrency(budgetStatus.budget.monthlyLimit - budgetStatus.projectedMonthlySpending)} under)"
                    },
                style = MaterialTheme.typography.bodySmall,
                color = if (budgetStatus.projectedMonthlySpending > budgetStatus.budget.monthlyLimit) {
                    MaterialTheme.colorScheme.error
                } else {
                    Color(0xFF2E7D32)
                }
            )
        }
    }
}

@Composable
private fun EmptyBudgetState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "💰",
            style = MaterialTheme.typography.displayLarge
        )
        Text(
            text = "No budgets yet",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Create a budget to track your spending",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorBudgetState(
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
            text = "Error loading budgets",
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
