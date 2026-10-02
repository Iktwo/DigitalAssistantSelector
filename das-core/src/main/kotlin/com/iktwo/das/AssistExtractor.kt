package com.iktwo.das

import android.app.assist.AssistStructure
import android.graphics.Rect
import android.view.View

/**
 * Extracts text regions with absolute screen bounds from an [AssistStructure]
 * captured while the app is configured as the digital assistant.
 */
object AssistExtractor {

    data class ExtractionResult(
        val regions: List<TextRegion>,
        val debug: String
    )

    /**
     * Extracts all visible text regions with valid bounds from the assist structure.
     */
    fun extractTextRegions(structure: AssistStructure): List<TextRegion> {
        return extractTextRegionsDetailed(structure).regions
    }

    /**
     * Same as [extractTextRegions] but also returns debug information about the
     * traversal, useful when a screen unexpectedly yields no regions.
     */
    fun extractTextRegionsDetailed(structure: AssistStructure): ExtractionResult {
        val regions = mutableListOf<TextRegion>()
        val debug = StringBuilder()
        val windowCount = structure.windowNodeCount
        debug.append("Windows: $windowCount")

        var totalNodes = 0
        var textNodes = 0

        for (i in 0 until windowCount) {
            val windowNode = structure.getWindowNodeAt(i)
            val windowLeft = windowNode.left
            val windowTop = windowNode.top
            debug.append("\nWin[$i]: title=${windowNode.title}, pos=($windowLeft,$windowTop)")

            val countBefore = regions.size
            totalNodes += traverseNodeWithBounds(windowNode.rootViewNode, windowLeft, windowTop, regions)
            textNodes += regions.size - countBefore
        }

        val filtered = regions.filter {
            it.text.isNotBlank() && it.bounds.width() > 0 && it.bounds.height() > 0
        }
        debug.append("\nTotal nodes: $totalNodes, text nodes: $textNodes, after filter: ${filtered.size}")
        if (filtered.isNotEmpty()) {
            val first = filtered.first()
            debug.append("\nFirst: \"${first.text.take(30)}\" at ${first.bounds}")
        }

        return ExtractionResult(regions = filtered, debug = debug.toString())
    }

    /**
     * Convenience helper that flattens every visible text (and content
     * description) in the structure into a single string.
     */
    fun extractText(structure: AssistStructure): String {
        val builder = StringBuilder()
        for (i in 0 until structure.windowNodeCount) {
            traverseNode(structure.getWindowNodeAt(i).rootViewNode, builder)
        }
        return builder.toString().trim()
    }

    private fun traverseNode(node: AssistStructure.ViewNode?, builder: StringBuilder) {
        if (node == null) return

        val text = node.text?.toString()?.trim()
        if (!text.isNullOrEmpty()) {
            builder.append(text).append(" ")
        }

        val description = node.contentDescription?.toString()?.trim()
        if (!description.isNullOrEmpty() && description != text) {
            builder.append(description).append(" ")
        }

        for (i in 0 until node.childCount) {
            traverseNode(node.getChildAt(i), builder)
        }
    }

    private fun traverseNodeWithBounds(
        node: AssistStructure.ViewNode?,
        parentAbsLeft: Int,
        parentAbsTop: Int,
        regions: MutableList<TextRegion>
    ): Int {
        if (node == null) return 0
        var count = 1
        val absLeft = parentAbsLeft + node.left - node.scrollX
        val absTop = parentAbsTop + node.top - node.scrollY

        val isVisible = node.visibility == View.VISIBLE &&
                node.width > 0 && node.height > 0

        val text = node.text?.toString()?.trim()
        if (!text.isNullOrEmpty() && isVisible) {
            regions.add(
                TextRegion(
                    text = text,
                    bounds = Rect(absLeft, absTop, absLeft + node.width, absTop + node.height)
                )
            )
        } else {
            val description = node.contentDescription?.toString()?.trim()
            if (!description.isNullOrEmpty() && isVisible) {
                regions.add(
                    TextRegion(
                        text = description,
                        bounds = Rect(absLeft, absTop, absLeft + node.width, absTop + node.height)
                    )
                )
            }
        }

        for (i in 0 until node.childCount) {
            count += traverseNodeWithBounds(node.getChildAt(i), absLeft, absTop, regions)
        }
        return count
    }
}
