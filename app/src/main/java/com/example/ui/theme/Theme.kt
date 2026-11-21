package com.example.ui.theme

import android.app.Activity
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
    val themeMode: ThemeMode,
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
    themeMode = ThemeMode.DARK,
    isDark = true,
    glassSurface = Color(0xD90E1629),
    glassSurfaceElevated = Color(0xF015203B),
    glassBorder = Color(0x3D4C8DFF),
    cyanGlow = CyanGlow,
    electricBlue = ElectricBlue,
    success = EmeraldSuccess,
    warning = AmberWarning,
    danger = CrimsonError,
    info = IndigoInfo,
    cardGradientStart = Color(0xFF0E1B3A),
    cardGradientEnd = Color(0xFF040814),
    subtleText = DarkSecondaryText
)

val NavyExtraColors = HKPayExtraColors(
    themeMode = ThemeMode.NAVY,
    isDark = true,
    glassSurface = Color(0xCC0C2058),
    glassSurfaceElevated = Color(0xEB132D75),
    glassBorder = Color(0x4700E5FF),
    cyanGlow = CyanGlow,
    electricBlue = ElectricBlueBright,
    success = EmeraldSuccess,
    warning = AmberWarning,
    danger = CrimsonError,
    info = IndigoInfo,
    cardGradientStart = Color(0xFF0D286B),
    cardGradientEnd = Color(0xFF030B29),
    subtleText = SteelText
)

val LightExtraColors = HKPayExtraColors(
    themeMode = ThemeMode.LIGHT,
    isDark = false,
    glassSurface = Color(0xF8FFFFFF),
    glassSurfaceElevated = Color(0xFFFFFFFF),
    glassBorder = LightBorder,
    cyanGlow = LightCyanAccent,
    electricBlue = LightElectricBlue,
    success = Color(0xFF008A5B),
    warning = Color(0xFFB86E00),
    danger = Color(0xFFC9203D),
    info = Color(0xFF1D4ED8),
    cardGradientStart = Color(0xFF0A1D4D),
    cardGradientEnd = Color(0xFF03081C),
    subtleText = LightMutedText
)

val LocalHKPayColors = staticCompositionLocalOf { DarkExtraColors }

private val DarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    primaryContainer = DarkSurfaceElevated,
    onPrimaryContainer = CyanSoft,
    secondary = CyanGlow,
    onSecondary = DarkBackground,
    secondaryContainer = Color(0xFF082D44),
    onSecondaryContainer = CyanSoft,
    tertiary = IndigoInfo,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    outlineVariant = Color(0xFF1B2742),
    error = CrimsonError,
    onError = Color.White
)

private val NavyColorScheme = darkColorScheme(
    primary = ElectricBlueBright,
    onPrimary = Color.White,
    primaryContainer = NavyElevated,
    onPrimaryContainer = CyanSoft,
    secondary = CyanGlow,
    onSecondary = NavyVoid,
    secondaryContainer = Color(0xFF08395C),
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
    outlineVariant = Color(0xFF193275),
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
    outlineVariant = Color(0xFFDCE7F5),
    error = Color(0xFFC9203D),
    onError = Color.White
)

@Composable
fun HKPayTheme(
    themeMode: ThemeMode = ThemeMode.NAVY,
    content: @Composable () -> Unit
) {
    val useDarkSystemBars = themeMode != ThemeMode.LIGHT

    val colorScheme = when (themeMode) {
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.NAVY -> NavyColorScheme
        ThemeMode.LIGHT -> LightColorScheme
    }
    val extraColors = when (themeMode) {
        ThemeMode.DARK -> DarkExtraColors
        ThemeMode.NAVY -> NavyExtraColors
        ThemeMode.LIGHT -> LightExtraColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = !useDarkSystemBars
                controller.isAppearanceLightNavigationBars = !useDarkSystemBars
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
