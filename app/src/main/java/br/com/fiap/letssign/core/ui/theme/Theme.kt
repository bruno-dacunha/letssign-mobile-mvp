package br.com.fiap.letssign.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LetsSignLightColorScheme = lightColorScheme(
    primary = LetsSignPurplePrimary,
    onPrimary = SurfaceLight,
    primaryContainer = Color(0xFFE9E3FF),
    onPrimaryContainer = LetsSignPurpleDark,
    secondary = FiapPinkAccent,
    onSecondary = SurfaceLight,
    tertiary = LetsSignCyanAccent,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEEF2FF),
    onSurfaceVariant = TextSecondaryLight,
    error = StatusErrorRed,
    onError = SurfaceLight
)

@Composable
fun LetsSignTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LetsSignLightColorScheme,
        typography = LetsSignTypography,
        content = content
    )
}
