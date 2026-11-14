package com.obsidianlumina.home.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object LuminaColors {
    val CanvasBase = Color(0xFFF8FAFC)
    val TileOff = Color(0xFFFFFFFF)
    val TileOn = Color(0xFFF1F5F9)
    val RecessedWell = Color(0xFFEEF2F7)
    val BorderOff = Color(0xFFCBD5E1)
    val Hairline = Color(0xFFE2E8F0)

    val TextPrimary = Color(0xFF0F172A)
    val TextSecondary = Color(0xFF475569)
    val TextMuted = Color(0xFF64748B)

    val CyanSignal = Color(0xFF00E5FF)
    val CyanText = Color(0xFF006875)
    val CyanDarkText = Color(0xFF004F58)

    val AmberSignal = Color(0xFFFFB300)
    val AmberText = Color(0xFF7E5700)

    val EmeraldSignal = Color(0xFF00E676)
    val EmeraldText = Color(0xFF006D35)

    val EmergencyRed = Color(0xFFBA1A1A)
    val EmergencyBorder = Color(0xFFFF3D71)
}

private val LuminaColorScheme = lightColorScheme(
    primary = LuminaColors.CyanText,
    onPrimary = Color.White,
    secondary = LuminaColors.AmberText,
    onSecondary = Color.White,
    tertiary = LuminaColors.EmeraldText,
    background = LuminaColors.CanvasBase,
    onBackground = LuminaColors.TextPrimary,
    surface = LuminaColors.TileOff,
    onSurface = LuminaColors.TextPrimary,
    surfaceVariant = LuminaColors.TileOn,
    onSurfaceVariant = LuminaColors.TextMuted,
    outline = LuminaColors.BorderOff,
    error = LuminaColors.EmergencyRed
)

@Composable
fun ObsidianLuminaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LuminaColorScheme,
        content = content
    )
}
