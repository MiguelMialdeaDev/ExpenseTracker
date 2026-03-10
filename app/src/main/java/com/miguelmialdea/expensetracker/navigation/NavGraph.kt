package com.miguelmialdea.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import screen.AddExpenseScreen
import screen.HomeScreen

@Composable
fun ExpenseTrackerNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Home Screen
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToAdd = {
                    navController.navigate(Screen.AddExpense.createRoute())
                },
                onExpenseClick = { expenseId ->
                    navController.navigate(Screen.AddExpense.createRoute(expenseId))
                }
            )
        }

        // Add/Edit Expense Screen
        composable(
            route = Screen.AddExpense.route,
            arguments = listOf(
                navArgument("expenseId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getLong("expenseId")
            AddExpenseScreen(
                expenseId = if (expenseId == 0L) null else expenseId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
