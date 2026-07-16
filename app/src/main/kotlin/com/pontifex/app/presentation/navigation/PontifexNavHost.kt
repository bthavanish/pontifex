package com.pontifex.app.presentation.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pontifex.app.presentation.screens.onboarding.OnboardingScreen
import com.pontifex.app.presentation.screens.splash.SplashScreen
import com.pontifex.app.presentation.screens.terminal.TerminalScreen
import com.pontifex.app.presentation.screens.files.FileBrowserScreen
import com.pontifex.app.presentation.screens.hotlist.HotlistScreen
import com.pontifex.app.presentation.screens.settings.SettingsScreen

@Composable
fun PontifexNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() }
        ) {
            SplashScreen(
                onReady = {
                    navController.navigate(Screen.Terminal.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Onboarding.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { it / 4 }
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { -it / 4 }
            }
        ) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Screen.Terminal.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Terminal.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { it / 4 } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { -it / 4 } + fadeOut()
            }
        ) {
            TerminalScreen()
        }

        composable(
            route = Screen.Files.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { it / 4 } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { -it / 4 } + fadeOut()
            }
        ) {
            FileBrowserScreen()
        }

        composable(
            route = Screen.Hotlist.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { it / 4 } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { -it / 4 } + fadeOut()
            }
        ) {
            HotlistScreen()
        }

        composable(
            route = Screen.Settings.route,
            enterTransition = {
                slideInHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { it / 4 } + fadeIn()
            },
            exitTransition = {
                slideOutHorizontally(
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow
                    )
                ) { -it / 4 } + fadeOut()
            }
        ) {
            SettingsScreen()
        }
    }
}
