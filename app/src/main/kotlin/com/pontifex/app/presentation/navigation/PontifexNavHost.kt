package com.pontifex.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pontifex.app.presentation.screens.onboarding.OnboardingScreen
import com.pontifex.app.presentation.screens.terminal.TerminalScreen
import com.pontifex.app.presentation.screens.files.FileBrowserScreen
import com.pontifex.app.presentation.screens.hotlist.HotlistScreen
import com.pontifex.app.presentation.screens.settings.SettingsScreen

@Composable
fun PontifexNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Terminal.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Terminal.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Terminal.route) {
            TerminalScreen()
        }
        composable(Screen.Files.route) {
            FileBrowserScreen()
        }
        composable(Screen.Hotlist.route) {
            HotlistScreen()
        }
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
    }
}
