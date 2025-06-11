package com.example.solitaire.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solitaire.ui.screens.GameSelectionScreen
import com.example.solitaire.ui.screens.FreeCellScreen
import com.example.solitaire.ui.screens.SolitaireScreen

sealed class Screen(val route: String) {
    object GameSelection : Screen("game_selection")
    object FreeCell : Screen("free_cell")
    object Solitaire : Screen("solitaire")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.GameSelection.route) {
        composable(Screen.GameSelection.route) {
            GameSelectionScreen(
                onFreeCellClick = { navController.navigate(Screen.FreeCell.route) },
                onSolitaireClick = { navController.navigate(Screen.Solitaire.route) }
            )
        }
        composable(Screen.FreeCell.route) {
            FreeCellScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
        composable(Screen.Solitaire.route) {
            SolitaireScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
} 