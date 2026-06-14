package com.wifiguard.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val GuardBlack = Color(0xFF05070D)
val GuardPanel = Color(0xFF0B1220)
val GuardBlue = Color(0xFF071A2F)
val GuardNeon = Color(0xFF20F58B)
val GuardOrange = Color(0xFFFFA726)
val GuardRed = Color(0xFFFF5252)

private val DarkScheme = darkColorScheme(
    primary = GuardNeon,
    onPrimary = GuardBlack,
    secondary = Color(0xFF55A7FF),
    background = GuardBlack,
    surface = GuardPanel,
    surfaceVariant = GuardBlue,
    onBackground = Color(0xFFEAF5FF),
    onSurface = Color(0xFFEAF5FF),
    outline = Color(0xFF27405D),
    error = GuardRed
)

@Composable
fun WiFiGuardTheme(content: @Composable () -> Unit) {
    val background = animateColorAsState(if (isSystemInDarkTheme()) GuardBlack else GuardBlack, label = "theme")
    MaterialTheme(colorScheme = DarkScheme.copy(background = background.value), content = content)
}
