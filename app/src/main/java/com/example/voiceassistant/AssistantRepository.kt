package com.example.voiceassistant

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AssistantState {
    IDLE,
    WAKE_LISTENING,
    LISTENING,
    THINKING,
    SPEAKING
}

class AssistantRepository private constructor(context: Context) {
    
    private val _assistantState = MutableStateFlow(AssistantState.IDLE)
    val assistantState: StateFlow<AssistantState> = _assistantState.asStateFlow()
    
    private val _statusText = MutableStateFlow("Idle")
    val statusText: StateFlow<String> = _statusText.asStateFlow()
    
    private val _handsFreeEnabled = MutableStateFlow(false)
    val handsFreeEnabled: StateFlow<Boolean> = _handsFreeEnabled.asStateFlow()
    
    private val _followUpEnabled = MutableStateFlow(false)
    val followUpEnabled: StateFlow<Boolean> = _followUpEnabled.asStateFlow()
    
    private val _earconEnabled = MutableStateFlow(true)
    val earconEnabled: StateFlow<Boolean> = _earconEnabled.asStateFlow()
    
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    
    fun setState(state: AssistantState) {
        _assistantState.value = state
    }
    
    fun setStatusText(text: String) {
        _statusText.value = text
    }
    
    fun setHandsFreeEnabled(enabled: Boolean) {
        _handsFreeEnabled.value = enabled
    }
    
    fun setFollowUpEnabled(enabled: Boolean) {
        _followUpEnabled.value = enabled
    }
    
    fun setEarconEnabled(enabled: Boolean) {
        _earconEnabled.value = enabled
    }
    
    fun playEarcon() {
        if (_earconEnabled.value) {
            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 200)
        }
    }
    
    companion object {
        @Volatile
        private var INSTANCE: AssistantRepository? = null
        
        fun getInstance(context: Context): AssistantRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AssistantRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
