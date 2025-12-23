package com.ashkite.pictureclassification.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SageDark,
    onPrimary = Night,
    primaryContainer = Sage,
    onPrimaryContainer = Cream,
    secondary = TerracottaDark,
    onSecondary = Night,
    secondaryContainer = Terracotta,
    onSecondaryContainer = Cream,
    tertiary = SlateDark,
    onTertiary = Night,
    tertiaryContainer = Slate,
    onTertiaryContainer = Cream,
    background = Night,
    onBackground = Sand,
    surface = NightSurface,
    onSurface = Sand,
    surfaceVariant = NightVariant,
    onSurfaceVariant = Sandstone,
    outline = NightOutline,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = Sage,
    onPrimary = Cream,
    primaryContainer = SageLight,
    onPrimaryContainer = Ink,
    secondary = Terracotta,
    onSecondary = Cream,
    secondaryContainer = TerracottaLight,
    onSecondaryContainer = Ink,
    tertiary = Slate,
    onTertiary = Cream,
    tertiaryContainer = SlateLight,
    onTertiaryContainer = Ink,
    background = Sand,
    onBackground = Ink,
    surface = Cream,
    onSurface = Ink,
    surfaceVariant = Sandstone,
    onSurfaceVariant = InkSoft,
    outline = OutlineWarm,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight
)

@Composable
fun PictureClassificationTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (useDarkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
