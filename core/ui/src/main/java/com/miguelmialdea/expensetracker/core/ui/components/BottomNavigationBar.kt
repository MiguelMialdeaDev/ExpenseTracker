package com.miguelmialdea.expensetracker.core.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.miguelmialdea.expensetracker.core.ui.items.BottomNavItem

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) }
            )
        }
    }
}

@Composable
fun getBottomNavItems(): List<BottomNavItem> {
    return listOf(
        BottomNavItem(
            route = "home",
            label = "Home",
            icon = Icons.Default.Home
        ),
        BottomNavItem(
            route = "dashboard",
            label = "Dashboard",
            icon = Icons.AutoMirrored.Filled.List
        ),
        BottomNavItem(
            route = "budget",
            label = "Budget",
            icon = Icons.Filled.Star
        ),
        BottomNavItem(
            route = "filter",
            label = "Filter",
            icon = Icons.Default.Search
        )
    )
}
