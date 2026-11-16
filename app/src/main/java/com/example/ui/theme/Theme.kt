package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class AttendanceSemanticColors(
    val bgTop: Color,
    val bgBottom: Color,
    val cardSurface: Color,
    val cardBorder: Color,
    val headerAttendance: Color,
    val headerEmployees: Color,
    val headerReports: Color,
    val headerSettings: Color,
    val present: Color,
    val presentSolid: Color,
    val presentContainer: Color,
    val onPresentContainer: Color,
    val absent: Color,
    val absentSolid: Color,
    val absentContainer: Color,
    val onAbsentContainer: Color,
    val leave: Color,
    val leaveSolid: Color,
    val leaveContainer: Color,
    val onLeaveContainer: Color,
    val dayShift: Color,
    val dayShiftSolid: Color,
    val dayShiftContainer: Color,
    val onDayShiftContainer: Color,
    val nightShift: Color,
    val nightShiftSolid: Color,
    val nightShiftContainer: Color,
    val onNightShiftContainer: Color,
    val indigoContainer: Color,
    val onIndigoContainer: Color
)

private val LightSemanticColors = AttendanceSemanticColors(
    bgTop = SlateLightBgTop,
    bgBottom = SlateLightBgBottom,
    cardSurface = SlateLightSurface.copy(alpha = 0.88f),
    cardBorder = Color.White.copy(alpha = 0.75f),
    headerAttendance = IndigoHeader,
    headerEmployees = EmeraldAccent,
    headerReports = PurpleAccent,
    headerSettings = SettingsHeaderLight,
    present = EmeraldAccent,
    presentSolid = EmeraldAccent,
    presentContainer = EmeraldContainerLight,
    onPresentContainer = Color(0xFF065F46),
    absent = RoseAbsent,
    absentSolid = RoseAbsent,
    absentContainer = RoseContainerLight,
    onAbsentContainer = Color(0xFF9F1239),
    leave = AmberLeave,
    leaveSolid = AmberLeave,
    leaveContainer = AmberContainerLight,
    onLeaveContainer = Color(0xFF92400E),
    dayShift = SkyShift,
    dayShiftSolid = SkyShift,
    dayShiftContainer = SkyContainerLight,
    onDayShiftContainer = Color(0xFF075985),
    nightShift = PurpleAccent,
    nightShiftSolid = PurpleAccent,
    nightShiftContainer = PurpleContainerLight,
    onNightShiftContainer = Color(0xFF6B21A8),
    indigoContainer = IndigoContainerLight,
    onIndigoContainer = Color(0xFF3730A3)
)

private val DarkSemanticColors = AttendanceSemanticColors(
    bgTop = SlateDarkBgTop,
    bgBottom = SlateDarkBgBottom,
    cardSurface = SlateDarkSurface.copy(alpha = 0.94f),
    cardBorder = Color(0xFF475569).copy(alpha = 0.55f),
    headerAttendance = IndigoHeaderDark,
    headerEmployees = Color(0xFF047857),
    headerReports = Color(0xFF7E22CE),
    headerSettings = SettingsHeaderDark,
    present = Color(0xFF34D399),
    presentSolid = Color(0xFF047857),
    presentContainer = EmeraldContainerDark,
    onPresentContainer = Color(0xFF6EE7B7),
    absent = Color(0xFFFB7185),
    absentSolid = Color(0xFFE11D48),
    absentContainer = RoseContainerDark,
    onAbsentContainer = Color(0xFFFDA4AF),
    leave = Color(0xFFFBBF24),
    leaveSolid = Color(0xFFB45309),
    leaveContainer = AmberContainerDark,
    onLeaveContainer = Color(0xFFFCD34D),
    dayShift = Color(0xFF38BDF8),
    dayShiftSolid = Color(0xFF0369A1),
    dayShiftContainer = SkyContainerDark,
    onDayShiftContainer = Color(0xFF7DD3FC),
    nightShift = Color(0xFFC084FC),
    nightShiftSolid = Color(0xFF7E22CE),
    nightShiftContainer = PurpleContainerDark,
    onNightShiftContainer = Color(0xFFD8B4FE),
    indigoContainer = IndigoContainerDark,
    onIndigoContainer = Color(0xFFA5B4FC)
)

val LocalAttendanceColors = staticCompositionLocalOf { LightSemanticColors }

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary,
    onPrimary = Color.White,
    primaryContainer = IndigoContainerLight,
    onPrimaryContainer = Color(0xFF312E81),
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = EmeraldContainerLight,
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = PurpleAccent,
    onTertiary = Color.White,
    tertiaryContainer = PurpleContainerLight,
    onTertiaryContainer = Color(0xFF6B21A8),
    background = SlateLightBgTop,
    onBackground = SlateTextPrimaryLight,
    surface = SlateLightSurface,
    onSurface = SlateTextPrimaryLight,
    surfaceVariant = SlateLightSurfaceVariant,
    onSurfaceVariant = SlateTextSecondaryLight,
    outline = SlateLightBorder,
    error = RoseAbsent,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = IndigoContainerDark,
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = EmeraldVibrant,
    onSecondary = Color(0xFF022C22),
    secondaryContainer = EmeraldContainerDark,
    onSecondaryContainer = Color(0xFFA7F3D0),
    tertiary = PurpleVibrant,
    onTertiary = Color(0xFF2E1065),
    tertiaryContainer = PurpleContainerDark,
    onTertiaryContainer = Color(0xFFE9D5FF),
    background = SlateDarkBgTop,
    onBackground = SlateTextPrimaryDark,
    surface = SlateDarkSurface,
    onSurface = SlateTextPrimaryDark,
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = SlateTextSecondaryDark,
    outline = SlateDarkBorder,
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519)
)

private val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DiyathalawaAttendanceTheme(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val semanticColors = if (darkTheme) DarkSemanticColors else LightSemanticColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes
    ) {
        CompositionLocalProvider(
            LocalAttendanceColors provides semanticColors,
            LocalContentColor provides colorScheme.onBackground,
            content = content
        )
    }
}
