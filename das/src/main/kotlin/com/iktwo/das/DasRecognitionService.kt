package com.iktwo.das

import android.content.Intent
import android.speech.RecognitionService

/**
 * Stub [RecognitionService] required by some OEMs (notably Samsung) to fully
 * recognize the app as a digital assistant. Without it, the
 * "Analyze on-screen text", "Analyze on-screen images", and "Flash screen"
 * options may not appear in system settings.
 *
 * This service does not perform actual speech recognition.
 */
class DasRecognitionService : RecognitionService() {

    override fun onStartListening(intent: Intent?, callback: Callback?) {
        callback?.error(android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY)
    }

    override fun onCancel(listener: Callback?) {}

    override fun onStopListening(listener: Callback?) {}
}
