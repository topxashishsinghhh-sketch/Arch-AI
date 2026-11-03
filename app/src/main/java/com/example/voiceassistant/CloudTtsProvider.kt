package com.example.voiceassistant

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CloudTtsProvider : TtsProvider {
    override suspend fun speak(text: String): Flow<TtsEvent> = flow {
        emit(TtsEvent.Started)
        // Simulate cloud TTS
        kotlinx.coroutines.delay(500)
        emit(TtsEvent.Done)
    }
    
    override fun stop() {}
    
    override fun release() {}
}
