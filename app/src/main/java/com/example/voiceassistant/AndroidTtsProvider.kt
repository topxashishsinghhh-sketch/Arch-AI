package com.example.voiceassistant

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale
import java.util.UUID

class AndroidTtsProvider(private val context: Context) : TtsProvider {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    
    init {
        tts = TextToSpeech(context) { status ->
            isInitialized = (status == TextToSpeech.SUCCESS)
            if (isInitialized) {
                tts?.language = Locale.getDefault()
            }
        }
    }
    
    override suspend fun speak(text: String): Flow<TtsEvent> = callbackFlow {
        if (!isInitialized || text.isBlank()) {
            trySend(TtsEvent.Error("TTS not initialized"))
            close()
            return@callbackFlow
        }
        
        val utteranceId = UUID.randomUUID().toString()
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                trySend(TtsEvent.Started)
            }
            override fun onDone(utteranceId: String?) {
                trySend(TtsEvent.Done)
                close()
            }
            @Deprecated("Deprecated")
            override fun onError(utteranceId: String?) {
                trySend(TtsEvent.Error("TTS error"))
                close()
            }
            override fun onError(utteranceId: String?, errorCode: Int) {
                trySend(TtsEvent.Error("TTS error"))
                close()
            }
        })
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        
        awaitClose {}
    }
    
    override fun stop() {
        tts?.stop()
    }
    
    override fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
