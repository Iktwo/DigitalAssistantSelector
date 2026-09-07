package com.iktwo.das

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

/**
 * [VoiceInteractionSessionService] that creates [DasAssistSession] instances.
 * Referenced from the library's `das_voice_interaction_service.xml`.
 */
class DasAssistSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return DasAssistSession(this)
    }
}
