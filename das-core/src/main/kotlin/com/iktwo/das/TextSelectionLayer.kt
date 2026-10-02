package com.iktwo.das

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Full-screen layer that dims the screen, outlines every [TextRegion] and
 * reports taps. It holds no state: the host keeps the [selected] indices and
 * draws its own controls on top, or uses [DasAssistOverlay] for a complete UI.
 *
 * [regions] bounds are absolute screen coordinates (as [AssistExtractor]
 * returns them), so place the layer in a window that covers the whole screen.
 *
 * @param onToggle called with the index of the tapped region. When regions
 *   overlap, the smallest one under the finger wins.
 * @param onTapOutside called when the tap hits no region.
 */
@Composable
fun TextSelectionLayer(
    regions: List<TextRegion>,
    selected: Set<Int>,
    onToggle: (index: Int) -> Unit,
    onTapOutside: () -> Unit,
    modifier: Modifier = Modifier,
    theme: DasOverlayTheme = DasOverlayTheme(),
) {
    val currentRegions by rememberUpdatedState(regions)
    val currentOnToggle by rememberUpdatedState(onToggle)
    val currentOnTapOutside by rememberUpdatedState(onTapOutside)

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(theme.scrimColor)
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val index = currentRegions.indexAt(offset)
                    if (index >= 0) currentOnToggle(index) else currentOnTapOutside()
                }
            }
    ) {
        regions.forEachIndexed { index, region ->
            val isSelected = index in selected
            val color = if (isSelected) theme.selectedColor else theme.highlightColor
            val topLeft = Offset(region.bounds.left.toFloat(), region.bounds.top.toFloat())
            val size = Size(
                region.bounds.width().toFloat().coerceAtLeast(1f),
                region.bounds.height().toFloat().coerceAtLeast(1f)
            )
            drawRect(
                color = color.copy(alpha = if (isSelected) 0.3f else 0.12f),
                topLeft = topLeft,
                size = size
            )
            drawRect(
                color = color,
                topLeft = topLeft,
                size = size,
                style = Stroke(width = if (isSelected) 4f else 2f)
            )
        }
    }
}

/**
 * The text of the regions at [indices], in the order [AssistExtractor] found
 * them, joined by spaces.
 */
fun List<TextRegion>.textOf(indices: Set<Int>): String =
    indices.sorted().mapNotNull { getOrNull(it)?.text }.joinToString(" ")

/** Index of the smallest region containing [offset], or -1 when none does. */
internal fun List<TextRegion>.indexAt(offset: Offset): Int {
    val x = offset.x.toInt()
    val y = offset.y.toInt()
    return indices
        .filter { this[it].bounds.contains(x, y) }
        .minByOrNull { this[it].bounds.width().toLong() * this[it].bounds.height() }
        ?: -1
}
