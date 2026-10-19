package com.example.paochess.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.paochess.ui.screens.*
import com.example.paochess.ui.theme.*

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Learn : Screen("learn", "Learn", Icons.Filled.School)
    object Openings : Screen("openings", "Openings", Icons.Filled.MenuBook)
    object Play : Screen("play", "Play", Icons.Filled.PlayArrow)
    object Puzzles : Screen("puzzles", "Puzzles", Icons.Filled.Extension)
    object Notation : Screen("notation", "Notation", Icons.Filled.List)
}

@Composable
fun ChessNavGraph() {
    val navController = rememberNavController()
    val items = listOf(Screen.Learn, Screen.Openings, Screen.Play, Screen.Puzzles, Screen.Notation)

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            NavigationBar(containerColor = DarkSurface) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label, tint = if (currentRoute == screen.route) Orange else LightGrey) },
                        label = { Text(screen.label, fontSize = 10.sp, color = if (currentRoute == screen.route) Orange else LightGrey) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color(0xFF2A2A2A))
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.Learn.route, modifier = Modifier.padding(padding)) {
            composable(Screen.Learn.route) { LearnScreen() }
            composable(Screen.Openings.route) { PlaceholderScreen("Openings coming soon") }
            composable(Screen.Play.route) { PlaceholderScreen("Play vs AI coming soon") }
            composable(Screen.Puzzles.route) { PuzzlesScreen() }
            composable(Screen.Notation.route) { PlaceholderScreen("Notation sheet coming soon") }
        }
    }
}

@Composable
fun PlaceholderScreen(text: String) {
    Box(modifier = Modifier.fillMaxSize().background(DarkBg), contentAlignment = Alignment.Center) {
        Text(text, color = LightGrey, fontSize = 18.sp)
    }
}
