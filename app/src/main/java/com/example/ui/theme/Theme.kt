package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MinecraftDarkColorScheme = darkColorScheme(
    primary = GrassGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = GrassGreenDark,
    onPrimaryContainer = Color.White,
    secondary = DiamondCyan,
    onSecondary = ObsidianDark,
    secondaryContainer = DiamondCyanDark,
    onSecondaryContainer = Color.White,
    tertiary = GoldAccent,
    onTertiary = ObsidianDark,
    background = ObsidianDark,
    onBackground = TextPixelWhite,
    surface = BedrockSurface,
    onSurface = TextPixelWhite,
    surfaceVariant = DeepslateCard,
    onSurfaceVariant = TextPixelDim,
    outline = StoneOutline,
    error = RedstoneAccent
)

private val MinecraftLightColorScheme = lightColorScheme(
    primary = GrassGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F3B5),
    onPrimaryContainer = Color(0xFF0F380A),
    secondary = Color(0xFF0F8274),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA6EFE6),
    onSecondaryContainer = Color(0xFF003832),
    tertiary = Color(0xFFB8860B),
    onTertiary = Color.White,
    background = Color(0xFFF3F6F4),
    onBackground = Color(0xFF1B221E),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1B221E),
    surfaceVariant = Color(0xFFE2EBE2),
    onSurfaceVariant = Color(0xFF404A42),
    outline = Color(0xFF707E72),
    error = RedstoneAccent
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to dark immersive Minecraft vibe
    dynamicColor: Boolean = false, // Keep Minecraft branding consistent
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> MinecraftDarkColorScheme
        else -> MinecraftLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
