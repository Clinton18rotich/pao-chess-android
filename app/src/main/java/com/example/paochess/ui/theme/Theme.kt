package com.example.paochess.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Orange = Color(0xFFFCA311)
val DarkBg = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val White = Color(0xFFFFFFFF)
val LightGrey = Color(0xFFAAAAAA)
val Green = Color(0xFF4CAF50)
val Red = Color(0xFFF44336)

private val DarkColors = darkColorScheme(
    primary = Orange,
    onPrimary = Color.Black,
    background = DarkBg,
    onBackground = White,
    surface = DarkSurface,
    onSurface = White,
)

@Composable
fun PAOTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        content = content
    )
}
