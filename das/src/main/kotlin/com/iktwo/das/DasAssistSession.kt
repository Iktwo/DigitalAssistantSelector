package com.iktwo.das

import android.app.assist.AssistContent
import android.app.assist.AssistStructure
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.service.voice.VoiceInteractionSession
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import com.iktwo.das.core.R

/**
 * [VoiceInteractionSession] that shows a [DasAssistOverlay] over the current
 * screen with every detected text region highlighted and tappable. When the
 * user confirms the selection, the text is delivered through
 * [DasConfig.onTextSelected].
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
            lifecycleOwner.attach(this)
            setContent {
                val config = Das.config
                DasAssistOverlay(
                    regions = detectedRegions.value,
                    isSearching = isSearching.value,
                    error = structureError.value,
                    onClose = ::dismissSession,
                    onConfirm = { text ->
                        Das.deliverSelection(context, text)
                        dismissSession()
                    },
                    theme = config.theme,
                    title = config.title ?: stringResource(R.string.das_default_title),
                    actionLabel = config.actionLabel ?: stringResource(R.string.das_default_action)
                )
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
            detectedRegions.value = AssistExtractor.extractTextRegions(structure)
        } else {
            structureError.value = context.getString(R.string.das_screen_text_unavailable)
        }
        isSearching.value = false
    }

    override fun onDestroy() {
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        super.onDestroy()
    }
}
