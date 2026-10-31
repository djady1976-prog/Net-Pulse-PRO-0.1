package com.example.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemePreset(
    val id: String,
    val titleRo: String,
    val subtitleRo: String,
    val previewBg: Color,
    val previewPrimary: Color,
    val previewSecondary: Color,
    val isDark: Boolean = true
) {
    CYBER_DARK(
        id = "CYBER_DARK",
        titleRo = "Cyber Telemetry (Implicit)",
        subtitleRo = "Navy obsidian cu accente cyan electric și smarald",
        previewBg = CyberObsidian,
        previewPrimary = ElectricCyan,
        previewSecondary = CyberEmerald
    ),
    MATRIX_TERMINAL(
        id = "MATRIX_TERMINAL",
        titleRo = "Matrix Hacker Green",
        subtitleRo = "Consolă terminal verde fosfor cu contrast ridicat",
        previewBg = MatrixBlack,
        previewPrimary = MatrixNeonGreen,
        previewSecondary = MatrixLimeAccent
    ),
    RED_OPS_TACTICAL(
        id = "RED_OPS_TACTICAL",
        titleRo = "Red Team Tactical",
        subtitleRo = "Estetică militară nocturnă cu roșu laser și chihlimbar",
        previewBg = RedOpsBlack,
        previewPrimary = RedOpsPrimary,
        previewSecondary = RedOpsSecondary
    ),
    SYNTHWAVE_QUANTUM(
        id = "SYNTHWAVE_QUANTUM",
        titleRo = "Synthwave Quantum",
        subtitleRo = "Indigo cosmic cu neon magenta și cyan holografic",
        previewBg = SynthObsidian,
        previewPrimary = SynthMagenta,
        previewSecondary = SynthCyan
    ),
    SOLAR_COMMAND(
        id = "SOLAR_COMMAND",
        titleRo = "Solar Amber NOC",
        subtitleRo = "Centru de comandă bronz închis cu telemetrie aurie",
        previewBg = SolarBlack,
        previewPrimary = SolarGold,
        previewSecondary = SolarOrange
    ),
    ARCTIC_LIGHT(
        id = "ARCTIC_LIGHT",
        titleRo = "Arctic Daylight (Luminos)",
        subtitleRo = "Interfață luminoasă de laborator cu lizibilitate maximă",
        previewBg = LightBackground,
        previewPrimary = LightCyanPrimary,
        previewSecondary = LightEmeraldSecondary,
        isDark = false
    )
}

private val CyberDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = CyberObsidian,
    primaryContainer = CyberCardElevated,
    onPrimaryContainer = ElectricCyan,
    secondary = CyberEmerald,
    onSecondary = CyberObsidian,
    secondaryContainer = Color(0xFF0A3326),
    onSecondaryContainer = CyberEmerald,
    tertiary = AmberAlert,
    onTertiary = CyberObsidian,
    error = CrimsonIntrusion,
    onError = Color.White,
    background = CyberObsidian,
    onBackground = TextPrimaryDark,
    surface = CyberNavySurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = CyberCardSurface,
    onSurfaceVariant = TextSecondaryDark,
    outline = BorderSubtleDark
)

private val MatrixColorScheme = darkColorScheme(
    primary = MatrixNeonGreen,
    onPrimary = MatrixBlack,
    primaryContainer = Color(0xFF0F381C),
    onPrimaryContainer = MatrixNeonGreen,
    secondary = MatrixLimeAccent,
    onSecondary = MatrixBlack,
    secondaryContainer = Color(0xFF1F3806),
    onSecondaryContainer = MatrixLimeAccent,
    tertiary = AmberAlert,
    onTertiary = MatrixBlack,
    error = CrimsonIntrusion,
    onError = Color.White,
    background = MatrixBlack,
    onBackground = Color(0xFFE2FCE9),
    surface = MatrixSurface,
    onSurface = Color(0xFFE2FCE9),
    surfaceVariant = MatrixCard,
    onSurfaceVariant = Color(0xFF86C997),
    outline = MatrixBorder
)

private val RedOpsColorScheme = darkColorScheme(
    primary = RedOpsPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF471220),
    onPrimaryContainer = Color(0xFFFFB3C1),
    secondary = RedOpsSecondary,
    onSecondary = RedOpsBlack,
    secondaryContainer = Color(0xFF422600),
    onSecondaryContainer = RedOpsSecondary,
    tertiary = ElectricCyan,
    onTertiary = RedOpsBlack,
    error = CrimsonIntrusion,
    onError = Color.White,
    background = RedOpsBlack,
    onBackground = Color(0xFFFCEEF2),
    surface = RedOpsSurface,
    onSurface = Color(0xFFFCEEF2),
    surfaceVariant = RedOpsCard,
    onSurfaceVariant = Color(0xFFD499A7),
    outline = RedOpsBorder
)

private val SynthwaveColorScheme = darkColorScheme(
    primary = SynthMagenta,
    onPrimary = SynthObsidian,
    primaryContainer = Color(0xFF3E1147),
    onPrimaryContainer = Color(0xFFFFB0F3),
    secondary = SynthCyan,
    onSecondary = SynthObsidian,
    secondaryContainer = Color(0xFF083947),
    onSecondaryContainer = SynthCyan,
    tertiary = AmberAlert,
    onTertiary = SynthObsidian,
    error = CrimsonIntrusion,
    onError = Color.White,
    background = SynthObsidian,
    onBackground = Color(0xFFF6EEFF),
    surface = SynthSurface,
    onSurface = Color(0xFFF6EEFF),
    surfaceVariant = SynthCard,
    onSurfaceVariant = Color(0xFFB59FD8),
    outline = SynthBorder
)

private val SolarCommandColorScheme = darkColorScheme(
    primary = SolarGold,
    onPrimary = SolarBlack,
    primaryContainer = Color(0xFF3D3106),
    onPrimaryContainer = SolarGold,
    secondary = SolarOrange,
    onSecondary = SolarBlack,
    secondaryContainer = Color(0xFF401F04),
    onSecondaryContainer = SolarOrange,
    tertiary = CyberEmerald,
    onTertiary = SolarBlack,
    error = CrimsonIntrusion,
    onError = Color.White,
    background = SolarBlack,
    onBackground = Color(0xFFFFF8E7),
    surface = SolarSurface,
    onSurface = Color(0xFFFFF8E7),
    surfaceVariant = SolarCard,
    onSurfaceVariant = Color(0xFFC9B37E),
    outline = SolarBorder
)

private val ArcticLightColorScheme = lightColorScheme(
    primary = LightCyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCF4FF),
    onPrimaryContainer = Color(0xFF001F26),
    secondary = LightEmeraldSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD0FBE1),
    onSecondaryContainer = Color(0xFF00210D),
    tertiary = Color(0xFFB26A00),
    onTertiary = Color.White,
    error = Color(0xFFBA1A1A),
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFCBD5E1)
)

fun AppThemePreset.toColorScheme(): ColorScheme = when (this) {
    AppThemePreset.CYBER_DARK -> CyberDarkColorScheme
    AppThemePreset.MATRIX_TERMINAL -> MatrixColorScheme
    AppThemePreset.RED_OPS_TACTICAL -> RedOpsColorScheme
    AppThemePreset.SYNTHWAVE_QUANTUM -> SynthwaveColorScheme
    AppThemePreset.SOLAR_COMMAND -> SolarCommandColorScheme
    AppThemePreset.ARCTIC_LIGHT -> ArcticLightColorScheme
}

@Composable
fun NetPulseProTheme(
    themePreset: AppThemePreset = AppThemePreset.CYBER_DARK,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = themePreset.toColorScheme(),
        typography = Typography,
        content = content
    )
}
