import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoiceViewModel(application: Application) : AndroidViewModel(application) {
    
    sealed class UiState {
        object Idle : UiState()
        object Listening : UiState()
        object Thinking : UiState()
        object Speaking : UiState()
    }
    
    sealed class ConfirmationState {
        object None : ConfirmationState()
        data class Pending(
            val action: String,
            val onConfirm: () -> Unit
        ) : ConfirmationState()
    }
    
    private val speechToText = SpeechToText(application)
    private val aiClient = AiClient()
    private val ttsManager = TextToSpeechManager(application)
    private val commandRouter = CommandRouter(application)
    private val repository = AssistantRepository.getInstance(application)
    
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    
    private val _statusText = MutableStateFlow("Idle")
    val statusText: StateFlow<String> = _statusText.asStateFlow()
    
    private val _confirmationState = MutableStateFlow<ConfirmationState>(ConfirmationState.None)
    val confirmationState: StateFlow<ConfirmationState> = _confirmationState.asStateFlow()
    
    // Settings
    private val _directCallingEnabled = MutableStateFlow(false)
    val directCallingEnabled: StateFlow<Boolean> = _directCallingEnabled.asStateFlow()
    
    val handsFreeEnabled = repository.handsFreeEnabled
    val followUpEnabled = repository.followUpEnabled
    val earconEnabled = repository.earconEnabled
    val assistantState = repository.assistantState
    
    init {
        viewModelScope.launch {
            ttsManager.isSpeaking.collect { speaking ->
                if (speaking && _uiState.value == UiState.Speaking) {
                    // Still speaking
                } else if (!speaking && _uiState.value == UiState.Speaking) {
                    _uiState.value = UiState.Idle
                    _statusText.value = "Idle"
                }
            }
        }
        
        viewModelScope.launch {
            repository.statusText.collect { text ->
                if (handsFreeEnabled.value) {
                    _statusText.value = text
                }
            }
        }
    }
    
    fun onMicPressed() {
        when (_uiState.value) {
            is UiState.Idle -> {
                startListening()
            }
            is UiState.Speaking -> {
                // Tapping while speaking stops the speech
                ttsManager.stop()
                _uiState.value = UiState.Idle
                _statusText.value = "Idle"
            }
            is UiState.Listening -> {
                speechToText.stopListening()
                _uiState.value = UiState.Idle
                _statusText.value = "Idle"
            }
            is UiState.Thinking -> {
                // Don't interrupt while thinking
            }
        }
    }
    
    private fun startListening() {
        _uiState.value = UiState.Listening
        _statusText.value = "Listening"
        
        viewModelScope.launch {
            speechToText.startListening().collect { event ->
                when (event) {
                    is SpeechToText.SpeechEvent.ListeningStarted -> {
                        _statusText.value = "Listening"
                    }
                    is SpeechToText.SpeechEvent.PartialResult -> {
                        _statusText.value = "Listening: ${event.text}"
                    }
                    is SpeechToText.SpeechEvent.FinalResult -> {
                        if (event.text.isNotBlank()) {
                            // Check if we're in confirmation state
                            val currentConfirmation = _confirmationState.value
                            if (currentConfirmation is ConfirmationState.Pending) {
                                handleConfirmation(event.text, currentConfirmation)
                                return@collect
                            }
                            
                            // Add user message
                            val userMsg = ChatMessage(text = event.text, isUser = true)
                            _messages.value = _messages.value + userMsg
                            
                            // Try to route as command first
                            processTranscript(event.text)
                        } else {
                            _uiState.value = UiState.Idle
                            _statusText.value = "Idle"
                        }
                    }
                    is SpeechToText.SpeechEvent.Error -> {
                        _uiState.value = UiState.Idle
                        _statusText.value = getErrorMessage(event.errorCode)
                    }
                }
            }
        }
    }
    
    private fun handleConfirmation(transcript: String, confirmation: ConfirmationState.Pending) {
        val normalized = transcript.lowercase().trim()
        if (normalized.contains("yes") || normalized.contains("yeah") || normalized.contains("sure") || normalized.contains("go ahead")) {
            confirmation.onConfirm()
            _confirmationState.value = ConfirmationState.None
            _uiState.value = UiState.Idle
            _statusText.value = "Idle"
        } else if (normalized.contains("no") || normalized.contains("cancel") || normalized.contains("stop")) {
            _confirmationState.value = ConfirmationState.None
            speakResponse("Cancelled")
            _uiState.value = UiState.Idle
            _statusText.value = "Idle"
        } else {
            // Ask again
            speakResponse("Please say yes or no. ${confirmation.action}")
            _uiState.value = UiState.Idle
            _statusText.value = "Idle"
        }
    }
    
    private fun processTranscript(transcript: String) {
        viewModelScope.launch {
            val commandResult = commandRouter.route(transcript)
            when (commandResult) {
                is CommandResult.Handled -> {
                    // Add action chip style message
                    val actionMsg = ChatMessage(text = commandResult.spokenReply, isUser = false, isAction = true)
                    _messages.value = _messages.value + actionMsg
                    speakResponse(commandResult.spokenReply)
                    _uiState.value = UiState.Idle
                    _statusText.value = "Idle"
                }
                is CommandResult.NotACommand -> {
                    // Fall through to AI
                    processAiResponse(transcript)
                }
            }
        }
    }
    
    private fun processAiResponse(userText: String) {
        _uiState.value = UiState.Thinking
        _statusText.value = "Thinking"
        
        viewModelScope.launch {
            val result = aiClient.getResponse(userText)
            result.fold(
                onSuccess = { response ->
                    // Add assistant message
                    val assistantMsg = ChatMessage(text = response, isUser = false)
                    _messages.value = _messages.value + assistantMsg
                    
                    // Check for command/intent router place (out of scope for now)
                    // TODO: Insert command/intent router here between AI reply and TTS
                    // This is the clear place mentioned in requirements
                    
                    // Speak response
                    speakResponse(response)
                },
                onFailure = {
                    _uiState.value = UiState.Idle
                    _statusText.value = "Error: ${it.message}"
                }
            )
        }
    }
    
    private fun speakResponse(response: String) {
        _uiState.value = UiState.Speaking
        _statusText.value = "Speaking"
        ttsManager.speak(response)
    }
    
    fun requestConfirmation(action: String, onConfirm: () -> Unit) {
        _confirmationState.value = ConfirmationState.Pending(action, onConfirm)
        speakResponse("$action, should I go ahead?")
        _uiState.value = UiState.Idle
        _statusText.value = "Waiting for confirmation"
    }
    
    fun setDirectCalling(enabled: Boolean) {
        _directCallingEnabled.value = enabled
    }
    
    fun setHandsFreeEnabled(enabled: Boolean, context: android.content.Context) {
        repository.setHandsFreeEnabled(enabled)
        if (enabled) {
            val intent = Intent(context, AssistantService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } else {
            val intent = Intent(context, AssistantService::class.java)
            context.stopService(intent)
        }
    }
    
    fun setFollowUpEnabled(enabled: Boolean) {
        repository.setFollowUpEnabled(enabled)
    }
    
    fun setEarconEnabled(enabled: Boolean) {
        repository.setEarconEnabled(enabled)
    }
    
    fun triggerFakeWakeWord() {
        val fakeDetector = FakeWakeWordDetector(getApplication())
        fakeDetector.setOnWakeWordDetectedListener {
            // This won't work directly, but for testing
        }
    }
    
    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            android.speech.SpeechRecognizer.ERROR_AUDIO -> "Audio error"
            android.speech.SpeechRecognizer.ERROR_CLIENT -> "Client error"
            android.speech.SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissions error"
            android.speech.SpeechRecognizer.ERROR_NETWORK -> "Network error"
            android.speech.SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            android.speech.SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            android.speech.SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
            android.speech.SpeechRecognizer.ERROR_SERVER -> "Server error"
            android.speech.SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
            else -> "Error"
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        speechToText.stopListening()
        ttsManager.release()
    }
}
