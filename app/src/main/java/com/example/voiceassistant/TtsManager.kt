package com.example.voiceassistant

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class TtsManager(context: Context) {
    private val androidTts = AndroidTtsProvider(context)
    private val cloudTts = CloudTtsProvider()
    private var useCloud = false
    
    fun setUseCloud(use: Boolean) {
        useCloud = use
    }
    
    suspend fun speak(text: String): Flow<TtsEvent> {
        return if (useCloud) {
            cloudTts.speak(text).catch {
                // Fallback to Android TTS
                androidTts.speak(text)
            }
        } else {
            androidTts.speak(text)
        }
    }
    
    fun stop() {
        androidTts.stop()
        cloudTts.stop()
    }
    
    fun release() {
        androidTts.release()
        cloudTts.release()
    }
}
