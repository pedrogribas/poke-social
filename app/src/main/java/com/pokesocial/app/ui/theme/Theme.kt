package com.pokesocial.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val IgScheme = lightColorScheme(
    primary = IgBlack,
    onPrimary = IgWhite,
    secondary = IgBlue,
    onSecondary = IgWhite,
    background = IgWhite,
    onBackground = IgBlack,
    surface = IgWhite,
    onSurface = IgBlack,
    surfaceVariant = IgBg,
    onSurfaceVariant = IgGray,
    outline = IgLightGray,
    error = IgHeart
)

@Composable
fun PokeSocialTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = IgScheme,
        typography = Typography,
        content = content
    )
}
