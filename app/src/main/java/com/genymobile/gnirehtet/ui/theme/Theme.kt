package com.genymobile.gnirehtet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.ViewCompat

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

enum class Theme(val displayName: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark");

    @Composable
    fun isDarkTheme(): Boolean {
        return when (this) {
            SYSTEM -> isSystemInDarkTheme()
            LIGHT -> false
            DARK -> true
        }
    }

    @Composable
    fun toColorScheme(): ColorScheme {
        return when (this) {
            SYSTEM -> if (isSystemInDarkTheme()) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
            LIGHT -> LightColorScheme
            DARK -> DarkColorScheme
        }
    }
}

@Composable
fun GnirehtetXTheme(
    theme: Theme,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (theme.isDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> theme.toColorScheme()
    }
    val view = LocalView.current

    if (!view.isInEditMode) {
        val isDarkTheme = theme.isDarkTheme()

        SideEffect {
            (view.context as Activity).window.statusBarColor = colorScheme.primary.toArgb()
            ViewCompat.getWindowInsetsController(view)?.isAppearanceLightStatusBars = isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
