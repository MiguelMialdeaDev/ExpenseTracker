package dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import models.BudgetModel
import models.CategoryModel

@Composable
fun AddBudgetDialog(
    onDismiss: () -> Unit,
    onSave: (BudgetModel) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<CategoryModel?>(null) }
    var monthlyLimitText by remember { mutableStateOf("") }
    var isCategoryDropdownExpanded by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }

    // Validation
    val isValidAmount = monthlyLimitText.isNotEmpty() &&
        monthlyLimitText.toDoubleOrNull() != null &&
        monthlyLimitText.toDouble() > 0

    val canSave = selectedCategory != null && isValidAmount

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Create Budget",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Selector
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Box {
                    OutlinedTextField(
                        value = selectedCategory?.let { getCategoryDisplay(it) } ?: "Select category",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCategoryDropdownExpanded = true },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown"
                            )
                        },
                        enabled = false,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        leadingIcon = selectedCategory?.let { category ->
                            {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(category.color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category.emoji,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        }
                    )

                    DropdownMenu(
                        expanded = isCategoryDropdownExpanded,
                        onDismissRequest = { isCategoryDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CategoryModel.entries.forEach { category ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(category.color.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = category.emoji,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                        Text(
                                            text = category.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                },
                                onClick = {
                                    selectedCategory = category
                                    isCategoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Monthly Limit Input
                Text(
                    text = "Monthly Limit",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = monthlyLimitText,
                    onValueChange = { newValue ->
                        // Validation: only allow numbers and one decimal point
                        val regex = Regex("^\\d*\\.?\\d{0,2}$")
                        if (newValue.isEmpty() || newValue.matches(regex)) {
                            monthlyLimitText = newValue
                            amountError = when {
                                newValue.isEmpty() -> null
                                newValue.toDoubleOrNull() == null -> "Invalid amount"
                                newValue.toDouble() <= 0 -> "Amount must be greater than 0"
                                newValue.toDouble() > 999999.99 -> "Amount too large"
                                else -> null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Enter monthly budget") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    isError = amountError != null,
                    supportingText = amountError?.let {
                        { Text(it) }
                    },
                    prefix = { Text("$") }
                )

                // Info text
                Text(
                    text = "This is the maximum amount you want to spend in this category per month.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    selectedCategory?.let { category ->
                        val budget = BudgetModel(
                            category = category,
                            monthlyLimit = monthlyLimitText.toDouble()
                        )
                        onSave(budget)
                        onDismiss()
                    }
                },
                enabled = canSave
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun getCategoryDisplay(category: CategoryModel): String {
    return "${category.emoji} ${category.name}"
}

// Extension properties for CategoryModel
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
