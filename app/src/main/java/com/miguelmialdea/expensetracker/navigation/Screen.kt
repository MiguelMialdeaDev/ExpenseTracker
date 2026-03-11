package com.miguelmialdea.expensetracker.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Dashboard : Screen("dashboard")
    data object Filter : Screen("filter")
    data object AddExpense : Screen("add_expense/{expenseId}") {
        fun createRoute(expenseId: Long? = null): String {
            return if (expenseId != null && expenseId != 0L) {
                "add_expense/$expenseId"
            } else {
                "add_expense/0"
            }
        }
    }
}
