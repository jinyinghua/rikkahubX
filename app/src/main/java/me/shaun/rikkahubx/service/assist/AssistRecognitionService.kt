package me.shaun.rikkahubx.service.assist

import android.speech.RecognitionService

class AssistRecognitionService : RecognitionService() {
    override fun onStartListening(intent: android.content.Intent?, listener: Callback?) {}
    override fun onCancel(listener: Callback?) {}
    override fun onStopListening(listener: Callback?) {}
}
