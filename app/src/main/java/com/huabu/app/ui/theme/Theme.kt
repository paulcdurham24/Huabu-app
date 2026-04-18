package com.huabu.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val HuabuDarkColorScheme = darkColorScheme(
    primary = HuabuViolet,              // #7B3FF2 — icon background purple
    onPrimary = HuabuOnSurface,
    primaryContainer = HuabuDeepPurple, // #5C2EBF — darker purple panels
    onPrimaryContainer = HuabuOnSurface,
    secondary = HuabuCoral,             // #E8614A — icon gradient coral
    onSecondary = HuabuOnSurface,
    secondaryContainer = HuabuSurface,
    onSecondaryContainer = HuabuOnSurface,
    tertiary = HuabuSkyBlue,            // #29B6F6 — icon "H" panel blue
    onTertiary = HuabuDarkBg,
    tertiaryContainer = HuabuCardBg2,
    onTertiaryContainer = HuabuOnSurface,
    error = HuabuError,
    background = HuabuDarkBg,           // #0E0818 — deep violet-black
    onBackground = HuabuOnSurface,
    surface = HuabuCardBg,              // #1C1336
    onSurface = HuabuOnSurface,
    surfaceVariant = HuabuCardBg2,
    onSurfaceVariant = HuabuSilver,
    outline = HuabuDivider,
    inverseSurface = HuabuOnSurface,
    inverseOnSurface = HuabuDarkBg,
    inversePrimary = HuabuCoral,
    surfaceTint = HuabuViolet,
    outlineVariant = HuabuDivider,
    scrim = HuabuDarkBg
)

@Composable
fun HuabuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = HuabuDarkColorScheme,
        typography = HuabuTypography,
        content = content
    )
}
