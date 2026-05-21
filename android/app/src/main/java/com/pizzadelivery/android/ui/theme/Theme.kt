package com.pizzadelivery.android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PizzaRed,
    onPrimary = White,
    primaryContainer = PizzaRedLight,
    onPrimaryContainer = White,
    secondary = PizzaOrange,
    onSecondary = White,
    secondaryContainer = PizzaYellow,
    onSecondaryContainer = Black,
    tertiary = PizzaGreen,
    onTertiary = White,
    background = GrayBackground,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = GrayLight,
    onSurfaceVariant = GrayDark,
    error = PizzaRed
)

private val DarkColorScheme = darkColorScheme(
    primary = PizzaRedLight,
    onPrimary = White,
    primaryContainer = PizzaRedDark,
    onPrimaryContainer = White,
    secondary = PizzaOrange,
    onSecondary = White,
    secondaryContainer = PizzaYellow,
    onSecondaryContainer = Black,
    tertiary = PizzaGreenLight,
    onTertiary = White,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = GrayDark,
    onSurfaceVariant = GrayMedium
)

@Composable
fun PizzaDeliveryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
