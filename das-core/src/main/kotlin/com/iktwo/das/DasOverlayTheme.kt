package com.iktwo.das

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Visual configuration for [TextSelectionLayer] and [DasAssistOverlay].
 */
data class DasOverlayTheme(
    val colorScheme: ColorScheme = darkColorScheme(),
    val highlightColor: Color = Color(0xFF8FF7F7),
    val selectedColor: Color = Color(0xFFFFCC00),
    val scrimColor: Color = Color.Black.copy(alpha = 0.4f)
)
