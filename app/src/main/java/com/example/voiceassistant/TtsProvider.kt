package com.example.voiceassistant

import kotlinx.coroutines.flow.Flow

interface TtsProvider {
    suspend fun speak(text: String): Flow<TtsEvent>
    fun stop()
    fun release()
}

sealed class TtsEvent {
    object Started : TtsEvent()
    object Done : TtsEvent()
    data class Error(val message: String) : TtsEvent()
}
