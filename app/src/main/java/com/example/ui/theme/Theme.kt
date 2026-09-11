package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentTheme: ToponAccentTheme = ToponAccentTheme.RESCUE_BLUE,
    content: @Composable () -> Unit
) {
    val primaryColor = if (darkTheme) accentTheme.primaryDark else accentTheme.primary

    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = primaryColor,
            onPrimary = Color.Black,
            primaryContainer = primaryColor.copy(alpha = 0.2f),
            onPrimaryContainer = primaryColor,
            secondary = ToponAlertAmber,
            onSecondary = Color.Black,
            tertiary = ToponVerifiedGreen,
            onTertiary = Color.Black,
            background = ToponNavyDark,
            onBackground = TextPrimaryDark,
            surface = ToponNavyCardDark,
            onSurface = TextPrimaryDark,
            surfaceVariant = ToponNavyCardElevatedDark,
            onSurfaceVariant = TextSecondaryDark,
            error = ToponAlertCoral,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = primaryColor,
            onPrimary = Color.White,
            primaryContainer = primaryColor.copy(alpha = 0.12f),
            onPrimaryContainer = primaryColor,
            secondary = ToponAlertAmber,
            onSecondary = Color.White,
            tertiary = ToponVerifiedGreen,
            onTertiary = Color.White,
            background = ToponNavyLight,
            onBackground = TextPrimaryLight,
            surface = ToponNavyCardLight,
            onSurface = TextPrimaryLight,
            surfaceVariant = ToponNavyCardElevatedLight,
            onSurfaceVariant = TextSecondaryLight,
            error = ToponAlertCoral,
            onError = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
