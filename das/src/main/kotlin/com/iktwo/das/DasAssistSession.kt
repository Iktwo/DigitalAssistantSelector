package com.iktwo.das

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.voice.VoiceInteractionSession
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner

/**
 * [VoiceInteractionSession] that shows an overlay over the current screen with
 * every detected text region highlighted and tappable. When the user confirms
 * the selection, the text is delivered through [DasConfig.onTextSelected].
 */
open class DasAssistSession(context: Context) : VoiceInteractionSession(context) {

    private val lifecycleOwner = SessionLifecycleOwner()
    private val handler = Handler(Looper.getMainLooper())

    private var detectedRegions = mutableStateOf<List<TextRegion>>(emptyList())
    private var isSearching = mutableStateOf(true)
    private var structureError = mutableStateOf<String?>(null)

    protected open fun dismissSession() {
        handler.post { hide() }
    }

    override fun onShow(args: Bundle?, showFlags: Int) {
        super.onShow(args, showFlags)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onHide() {
        super.onHide()
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onCreateContentView(): View {
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        return ComposeView(context).apply {
            setViewTreeLifecycleOwner(lifecycleOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setContent {
                MaterialTheme(colorScheme = Das.config.theme.colorScheme) {
                    DasAssistOverlay(
                        regions = detectedRegions.value,
                        isSearching = isSearching.value,
                        error = structureError.value,
                        theme = Das.config.theme,
                        title = Das.config.title,
                        actionLabel = Das.config.actionLabel,
                        onClose = ::dismissSession,
                        onConfirm = { text ->
                            Das.deliverSelection(context, text)
                            dismissSession()
                        }
                    )
                }
            }
        }
    }

    // API 33+
    override fun onHandleAssist(assistState: AssistState) {
        super.onHandleAssist(assistState)
        handleStructure(assistState.assistStructure)
    }

    // API < 33
    @Deprecated("Deprecated in parent class")
    @Suppress("DEPRECATION")
    override fun onHandleAssist(
        data: Bundle?,
        structure: AssistStructure?,
        content: AssistContent?
    ) {
        super.onHandleAssist(data, structure, content)
        handleStructure(structure)
    }

    protected open fun handleStructure(structure: AssistStructure?) {
        isSearching.value = true
        structureError.value = null
        if (structure != null) {
            val result = AssistExtractor.extractTextRegionsDetailed(structure)
            detectedRegions.value = result.regions
        } else {
            structureError.value =
                "Screen text not available. Enable \"Analyze on-screen text\" in " +
                        "Settings > Apps > Default apps > Digital assistant app."
        }
        isSearching.value = false
    }

    override fun onDestroy() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}

/**
 * The selection overlay rendered by [DasAssistSession]. Exposed publicly so it
 * can be re-used or previewed in isolation.
 */
@Composable
fun DasAssistOverlay(
    regions: List<TextRegion>,
    isSearching: Boolean,
    error: String?,
    theme: DasOverlayTheme,
    title: String,
    actionLabel: String,
    onClose: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedIndices by remember { mutableStateOf(emptySet<Int>()) }

    val selectedText = remember(selectedIndices, regions) {
        selectedIndices.sorted().mapNotNull { regions.getOrNull(it)?.text }.joinToString(" ")
    }
    val hasSelection = selectedText.isNotEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: scrim with tap detection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(theme.scrimColor)
                .pointerInput(regions) {
                    detectTapGestures { offset ->
                        try {
                            val hitIndex = regions.indexOfFirst { r ->
                                offset.x >= r.bounds.left && offset.x <= r.bounds.right &&
                                        offset.y >= r.bounds.top && offset.y <= r.bounds.bottom
                            }
                            selectedIndices = if (hitIndex >= 0) {
                                if (hitIndex in selectedIndices) {
                                    selectedIndices - hitIndex
                                } else {
                                    selectedIndices + hitIndex
                                }
                            } else {
                                onClose()
                                selectedIndices
                            }
                        } catch (_: Exception) {
                            // Ignore stale state race
                        }
                    }
                }
        )

        // Layer 2: highlight borders
        Canvas(modifier = Modifier.fillMaxSize()) {
            try {
                regions.forEachIndexed { index, region ->
                    val isSelected = index in selectedIndices
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
            } catch (_: Exception) {
                // Guard against concurrent modification during draw
            }
        }

        // Layer 3: close button
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 48.dp, end = 16.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .size(44.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
        }

        // Layer 4: bottom card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
                .navigationBarsPadding(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (hasSelection) {
                    Text(
                        text = selectedText,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    )
                } else {
                    Text(
                        text = when {
                            isSearching -> "Analyzing screen..."
                            error != null -> error
                            regions.isEmpty() -> "No screen text found."
                            else -> "Tap on highlighted screen text to select it."
                        },
                        color = if (error != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedIndices = if (selectedIndices.size == regions.size) {
                                emptySet()
                            } else {
                                regions.indices.toSet()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = regions.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(regions.isNotEmpty()).copy(
                            brush = SolidColor(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        )
                    ) {
                        Icon(
                            imageVector = if (selectedIndices.size == regions.size) {
                                Icons.Default.ClearAll
                            } else {
                                Icons.Default.SelectAll
                            },
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedIndices.size == regions.size) {
                                "Deselect All"
                            } else {
                                "Select All"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = { onConfirm(selectedText) },
                        modifier = Modifier.weight(1f),
                        enabled = hasSelection,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(actionLabel, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

internal class SessionLifecycleOwner : LifecycleOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    fun handleLifecycleEvent(event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

    fun performRestore(savedState: Bundle?) {
        savedStateRegistryController.performRestore(savedState)
    }
}
