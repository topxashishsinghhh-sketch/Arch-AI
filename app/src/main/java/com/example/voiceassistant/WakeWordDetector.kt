package com.example.voiceassistant

interface WakeWordDetector {
    fun start()
    fun stop()
    fun setOnWakeWordDetectedListener(listener: () -> Unit)
}
