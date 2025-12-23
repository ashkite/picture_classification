package com.ashkite.pictureclassification.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun AppScaffold(
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AppBackground()
        Scaffold(
            topBar = topBar,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            content = content
        )
    }
}

@Composable
private fun AppBackground() {
    val colors = listOf(
        MaterialTheme.colorScheme.background,
        MaterialTheme.colorScheme.surfaceVariant,
        MaterialTheme.colorScheme.background
    )
    val secondaryGlow = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
    val tertiaryGlow = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.32f)
    val primaryGlow = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            drawCircle(
                color = secondaryGlow,
                radius = width * 0.5f,
                center = Offset(width * 0.1f, height * 0.15f)
            )
            drawCircle(
                color = tertiaryGlow,
                radius = width * 0.6f,
                center = Offset(width * 0.92f, height * 0.08f)
            )
            drawCircle(
                color = primaryGlow,
                radius = width * 0.7f,
                center = Offset(width * 0.85f, height * 0.9f)
            )
        }
    }
}
