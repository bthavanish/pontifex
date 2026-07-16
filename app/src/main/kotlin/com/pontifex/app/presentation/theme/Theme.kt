package com.pontifex.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003355),
    primaryContainer = Color(0xFF004A78),
    onPrimaryContainer = Color(0xFFCEE5FF),
    secondary = Color(0xFFB4CAE5),
    onSecondary = Color(0xFF1F3347),
    secondaryContainer = Color(0xFF354A5E),
    onSecondaryContainer = Color(0xFFD0E6FF),
    tertiary = Color(0xFFCBBFFF),
    onTertiary = Color(0xFF32298C),
    tertiaryContainer = Color(0xFF4940A4),
    onTertiaryContainer = Color(0xFFE7DFFF),
    surface = Color(0xFF0E1117),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF42474E),
    onSurfaceVariant = Color(0xFFC2C7CE),
    surfaceContainer = Color(0xFF1A1D23),
    surfaceContainerHigh = Color(0xFF24282F),
    surfaceContainerHighest = Color(0xFF2F3239),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val DarkAmoledScheme = DarkColorScheme.copy(
    surface = Color.Black,
    surfaceContainer = Color(0xFF0D0D0D),
    surfaceContainerHigh = Color(0xFF151515),
    surfaceContainerHighest = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF005FA8),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCEE5FF),
    onPrimaryContainer = Color(0xFF001D35),
    secondary = Color(0xFF4D6277),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0E6FF),
    onSecondaryContainer = Color(0xFF071E30),
    tertiary = Color(0xFF5B52A0),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE7DFFF),
    onTertiaryContainer = Color(0xFF190061),
    surface = Color(0xFFF8F9FF),
    onSurface = Color(0xFF191C20),
    surfaceVariant = Color(0xFFDFE2EB),
    onSurfaceVariant = Color(0xFF43474E),
    surfaceContainer = Color(0xFFECEFF5),
    surfaceContainerHigh = Color(0xFFE6E9EF),
    surfaceContainerHighest = Color(0xFFE0E3E9),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

val PontifexShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

@Composable
fun PontifexTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    amoledBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> if (amoledBlack) DarkAmoledScheme else DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PontifexTypography,
        shapes = PontifexShapes,
        content = content
    )
}
