package com.iktwo.das

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Visual configuration for the assist selection overlay.
 */
data class DasOverlayTheme(
    val colorScheme: ColorScheme = darkColorScheme(),
    val highlightColor: Color = Color(0xFF8FF7F7),
    val selectedColor: Color = Color(0xFFFFCC00),
    val scrimColor: Color = Color.Black.copy(alpha = 0.4f)
)

/**
 * Configuration for DAS. Provide an instance to [Das.init] from your
 * [android.app.Application] (or any other early entry point).
 *
 * [onTextSelected] is invoked when the user confirms their selection in the
 * assist overlay. The default implementation copies the text to the clipboard
 * (when [copyToClipboard] is true) and forwards it to the app's launcher
 * activity as an [Intent.ACTION_PROCESS_TEXT] intent, so an activity with a
 * PROCESS_TEXT intent filter receives it like any other shared text.
 */
data class DasConfig(
    val title: String = "Assistant",
    val actionLabel: String = "Use Text",
    val copyToClipboard: Boolean = true,
    val clipboardLabel: String = "DAS Copied Text",
    val theme: DasOverlayTheme = DasOverlayTheme(),
    val onTextSelected: ((context: Context, text: String) -> Unit)? = null
)

/**
 * Entry point for configuring DAS.
 *
 * Call [init] once from your [android.app.Application.onCreate]:
 *
 * ```
 * class MyApp : Application() {
 *     override fun onCreate() {
 *         super.onCreate()
 *         Das.init(DasConfig(title = "My Assistant", actionLabel = "Use Text"))
 *     }
 * }
 * ```
 */
object Das {

    @Volatile
    internal var config: DasConfig = DasConfig()
        private set

    fun init(config: DasConfig) {
        this.config = config
    }

    internal fun deliverSelection(context: Context, text: String) {
        if (text.isBlank()) return

        if (config.copyToClipboard) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText(config.clipboardLabel, text))
        }

        config.onTextSelected?.invoke(context, text) ?: run {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            if (launchIntent != null) {
                launchIntent.action = Intent.ACTION_PROCESS_TEXT
                launchIntent.putExtra(Intent.EXTRA_PROCESS_TEXT, text)
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(launchIntent)
            }
        }
    }
}
