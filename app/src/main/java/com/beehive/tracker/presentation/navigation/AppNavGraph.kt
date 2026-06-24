package com.beehive.tracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.beehive.tracker.presentation.screen.hivedetail.HiveDetailScreen
import com.beehive.tracker.presentation.screen.home.HomeScreen
import com.beehive.tracker.presentation.screen.map.MapScreen
import com.beehive.tracker.presentation.screen.noteeditor.NoteEditorScreen
import com.beehive.tracker.presentation.screen.settings.SettingsScreen

// Tek navigasyon grafiği; tüm ekranlar burada tanımlanır.
// Faz 5'te sesli not rotası buraya eklenir.
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            HomeScreen(
                onApiaryClick = { apiaryId ->
                    navController.navigate(Screen.Map().buildRoute(apiaryId))
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.Map().route) { backStack ->
            val apiaryId = backStack.arguments?.getString("apiaryId") ?: return@composable
            MapScreen(
                apiaryId = apiaryId,
                onHiveClick = { hiveId ->
                    navController.navigate(Screen.HiveDetail().buildRoute(hiveId))
                },
                onBack = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
            )
        }

        composable(Screen.HiveDetail().route) { backStack ->
            val hiveId = backStack.arguments?.getString("hiveId") ?: return@composable
            HiveDetailScreen(
                hiveId = hiveId,
                onBack = { navController.popBackStack() },
                onAddNote = { id -> navController.navigate(Screen.NoteEditor().buildRoute(id)) },
            )
        }

        // Faz 3: not ekleme ekranı
        composable(Screen.NoteEditor().route) {
            NoteEditorScreen(onBack = { navController.popBackStack() })
        }
    }
}
