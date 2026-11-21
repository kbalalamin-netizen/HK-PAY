package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.data.model.ThemeMode

@Immutable
data class HKPayExtraColors(
    val isDark: Boolean,
    val glassSurface: Color,
    val glassSurfaceElevated: Color,
    val glassBorder: Color,
    val cyanGlow: Color,
    val electricBlue: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val cardGradientStart: Color,
    val cardGradientEnd: Color,
    val subtleText: Color
)

val DarkExtraColors = HKPayExtraColors(
    isDark = true,
    glassSurface = Color(0xCC0C1838),
    glassSurfaceElevated = Color(0xE6132450),
    glassBorder = Color(0x3D00E5FF),
    cyanGlow = CyanGlow,
    electricBlue = ElectricBlue,
    success = EmeraldSuccess,
    warning = AmberWarning,
    danger = CrimsonError,
    info = IndigoInfo,
    cardGradientStart = Color(0xFF0A1B47),
    cardGradientEnd = Color(0xFF030A1E),
    subtleText = SteelText
)

val LightExtraColors = HKPayExtraColors(
    isDark = false,
    glassSurface = Color(0xF2FFFFFF),
    glassSurfaceElevated = Color(0xFFFFFFFF),
    glassBorder = LightBorder,
    cyanGlow = LightCyanAccent,
    electricBlue = LightElectricBlue,
    success = Color(0xFF00A870),
    warning = Color(0xFFD98800),
    danger = Color(0xFFD92D4A),
    info = Color(0xFF2E5BFF),
    cardGradientStart = Color(0xFF0A1D4D),
    cardGradientEnd = Color(0xFF03081C),
    subtleText = LightMutedText
)

val LocalHKPayColors = staticCompositionLocalOf { DarkExtraColors }

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = NavyElevated,
    onPrimaryContainer = CyanSoft,
    secondary = CyanGlow,
    onSecondary = NavyVoid,
    secondaryContainer = Color(0xFF063751),
    onSecondaryContainer = CyanSoft,
    tertiary = IndigoInfo,
    onTertiary = Color.White,
    background = NavyVoid,
    onBackground = IceWhite,
    surface = NavyDeep,
    onSurface = IceWhite,
    surfaceVariant = NavySurface,
    onSurfaceVariant = SteelText,
    outline = NavyBorder,
    outlineVariant = Color(0xFF15254C),
    error = CrimsonError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = LightElectricBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE7FF),
    onPrimaryContainer = LightNavyText,
    secondary = LightCyanAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3F4FA),
    onSecondaryContainer = Color(0xFF003642),
    tertiary = IndigoInfo,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightNavyText,
    surface = LightSurface,
    onSurface = LightNavyText,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightMutedText,
    outline = LightBorder,
    outlineVariant = Color(0xFFE2EBF7),
    error = Color(0xFFD92D4A),
    onError = Color.White
)

@Composable
fun HKPayTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = if (useDark) DarkColorScheme else LightColorScheme
    val extraColors = if (useDark) DarkExtraColors else LightExtraColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDark
                controller.isAppearanceLightNavigationBars = !useDark
            }
        }
    }

    CompositionLocalProvider(LocalHKPayColors provides extraColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = HKPayTypography,
            content = content
        )
    }
}
