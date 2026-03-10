package components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import models.CategoryModel

@Composable
fun CategorySelector(
    selectedCategory: CategoryModel,
    onCategorySelected: (CategoryModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge
        )

        // Primera fila (FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                category = CategoryModel.FOOD,
                isSelected = selectedCategory == CategoryModel.FOOD,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                category = CategoryModel.TRANSPORT,
                isSelected = selectedCategory == CategoryModel.TRANSPORT,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
        }

        // Segunda fila (ENTERTAINMENT, SHOPPING)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                category = CategoryModel.ENTERTAINMENT,
                isSelected = selectedCategory == CategoryModel.ENTERTAINMENT,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                category = CategoryModel.SHOPPING,
                isSelected = selectedCategory == CategoryModel.SHOPPING,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
        }

        // Tercera fila (BILLS, HEALTH)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                category = CategoryModel.BILLS,
                isSelected = selectedCategory == CategoryModel.BILLS,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            CategoryChip(
                category = CategoryModel.HEALTH,
                isSelected = selectedCategory == CategoryModel.HEALTH,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
        }

        // Cuarta fila (OTHER)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChip(
                category = CategoryModel.OTHER,
                isSelected = selectedCategory == CategoryModel.OTHER,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            // Espacio vacío para mantener el layout
            Box(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CategoryChip(
    category: CategoryModel,
    isSelected: Boolean,
    onSelected: (CategoryModel) -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = { onSelected(category) },
        label = { Text(category.name) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = category.emoji,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        modifier = modifier
    )
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
