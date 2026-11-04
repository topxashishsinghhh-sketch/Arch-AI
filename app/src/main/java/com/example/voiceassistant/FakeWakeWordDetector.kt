package com.example.voiceassistant

import android.content.Context
import android.util.Log

class FakeWakeWordDetector(private val context: Context) : WakeWordDetector {
    private var listener: (() -> Unit)? = null
    
    override fun start() {
        Log.d("FakeWakeWordDetector", "Started (debug mode)")
    }
    
    override fun stop() {
        Log.d("FakeWakeWordDetector", "Stopped")
    }
    
    override fun setOnWakeWordDetectedListener(listener: () -> Unit) {
        this.listener = listener
    }
    
    fun triggerWakeWord() {
        listener?.invoke()
    }
}
