package com.example.smartshop.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.lightColorScheme

private val CleanLightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    secondary = IndigoPrimary,
    tertiary = SuccessGreen,
    background = PureWhite,
    surface = PureWhite,
    surfaceVariant = LightGraySurface,
    onPrimary = PureWhite,
    onSecondary = PureWhite,
    onBackground = DeepBlack,
    onSurface = DeepBlack,
    onSurfaceVariant = DarkGrayText,
    outline = BorderGray,
    error = AlertRed
)

@Composable
fun SmartShopTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = CleanLightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = IndigoPrimary.toArgb()
            window.navigationBarColor = PureWhite.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
