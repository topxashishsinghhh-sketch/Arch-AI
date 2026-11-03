package com.example.voiceassistant

import android.content.Context
import ai.picovoice.porcupine.Porcupine
import ai.picovoice.porcupine.PorcupineException
import ai.picovoice.porcupine.PorcupineManager
import ai.picovoice.porcupine.PorcupineManagerCallback

class PorcupineWakeWordDetector(private val context: Context) : WakeWordDetector {
    private var porcupineManager: PorcupineManager? = null
    private var listener: (() -> Unit)? = null
    
    override fun start() {
        try {
            val accessKey = BuildConfig.PORCUPINE_ACCESS_KEY
            if (accessKey.isEmpty()) {
                throw PorcupineException("Porcupine access key not set")
            }
            porcupineManager = PorcupineManager.Builder()
                .setAccessKey(accessKey)
                .setKeyword(Porcupine.BuiltInKeyword.PORCUPINE) // "Porcupine" as built-in, can change to custom
                .setSensitivity(0.5f)
                .build(context, object : PorcupineManagerCallback {
                    override fun invoke(keywordIndex: Int) {
                        listener?.invoke()
                    }
                })
            porcupineManager?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun stop() {
        try {
            porcupineManager?.stop()
            porcupineManager?.delete()
            porcupineManager = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun setOnWakeWordDetectedListener(listener: () -> Unit) {
        this.listener = listener
    }
}
