package com.iktwo.das

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

/**
 * Fallback entry point handling `ACTION_ASSIST` / `ACTION_VOICE_ASSIST`.
 * Having an activity with these intent filters is required for the system to
 * offer the app as an assistant candidate and to show the standard assist
 * sub-options (analyze on-screen text, images, flash screen).
 *
 * The actual selection overlay is handled by [DasAssistSession] through the
 * [DasVoiceInteractionService]; when this activity is invoked directly it
 * simply opens the host app's launcher activity.
 */
class DasAssistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(launchIntent)
        }
        finish()
    }
}
