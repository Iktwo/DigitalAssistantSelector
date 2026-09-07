package com.iktwo.das

import android.service.voice.VoiceInteractionService

/**
 * [VoiceInteractionService] advertised with `android:supportsAssist="true"` so
 * the system offers the host app as a digital assistant candidate.
 */
class DasVoiceInteractionService : VoiceInteractionService()
