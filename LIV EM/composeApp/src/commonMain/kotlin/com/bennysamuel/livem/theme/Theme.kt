package com.bennysamuel.livem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.zoho.accounts.nativeclientportaldemo.ui.theme.Pink40
import com.zoho.accounts.nativeclientportaldemo.ui.theme.Purple40
import com.zoho.accounts.nativeclientportaldemo.ui.theme.PurpleGrey40
import com.zoho.accounts.nativeclientportaldemo.ui.theme.Typography

val JournalDarkColorScheme = darkColorScheme(
    primary = Color(0xFFBFAE9F),
    onPrimary = Color(0xFF1C1A17),

    secondary = Color(0xFF9E948A),
    onSecondary = Color(0xFF1C1A17),

    tertiary = Color(0xFF7D746B),
    onTertiary = Color(0xFFEDEAE6),

    background = Color(0xFF121110),
    onBackground = Color(0xFFEDEAE6),

    surface = Color(0xFF1A1816),
    onSurface = Color(0xFFEDEAE6),

    surfaceVariant = Color(0xFF24211E),
    onSurfaceVariant = Color(0xFFB7B0A8),

    error = Color(0xFFD16666),
    onError = Color(0xFF3A0D0D),

    outline = Color(0xFF2E2A26),
    outlineVariant = Color(0xFF23201D)
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun LivEmTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = JournalDarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}