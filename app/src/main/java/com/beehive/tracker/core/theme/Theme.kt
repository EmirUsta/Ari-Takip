package com.beehive.tracker.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Amber700,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    primaryContainer = Amber50,
    onPrimaryContainer = Brown800,
    surface = androidx.compose.ui.graphics.Color.White,
    background = Amber50,
)

@Composable
fun KovanTakipTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}
