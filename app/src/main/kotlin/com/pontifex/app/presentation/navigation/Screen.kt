package com.pontifex.app.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    data object Splash : Screen("splash", "Splash", Icons.Filled.Terminal)
    data object Terminal : Screen("terminal", "Terminal", Icons.Filled.Terminal)
    data object Files : Screen("files", "Files", Icons.Filled.FolderOpen)
    data object Hotlist : Screen("hotlist", "Hotlist", Icons.Filled.Bookmark)
    data object Settings : Screen("settings", "Settings", Icons.Filled.Settings)
    data object Onboarding : Screen("onboarding", "Onboarding", Icons.Filled.Terminal)

    companion object {
        val bottomNavItems = listOf(Terminal, Files, Hotlist, Settings)
    }
}
