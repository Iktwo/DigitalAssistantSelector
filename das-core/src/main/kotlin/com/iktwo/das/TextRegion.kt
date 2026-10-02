package com.iktwo.das

import android.graphics.Rect

/**
 * A piece of text detected on the screen along with its absolute bounds.
 */
data class TextRegion(
    val text: String,
    val bounds: Rect
)
