package com.example.voiceassistant

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AssistantService : Service() {
    
    private val notificationId = 1001
    private val channelId = "assistant_service_channel"
    
    private lateinit var wakeWordDetector: WakeWordDetector
    private lateinit var speechToText: SpeechToText
    private lateinit var ttsManager: TextToSpeechManager
    private lateinit var commandRouter: CommandRouter
    private lateinit var aiClient: AiClient
    private lateinit var repository: AssistantRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var audioManager: AudioManager? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    
    override fun onCreate() {
        super.onCreate()
        repository = AssistantRepository.getInstance(this)
        speechToText = SpeechToText(this)
        ttsManager = TextToSpeechManager(this)
        commandRouter = CommandRouter(this)
        aiClient = AiClient()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Use fake detector for now, can switch to Porcupine
        wakeWordDetector = FakeWakeWordDetector(this)
        wakeWordDetector.setOnWakeWordDetectedListener {
            onWakeWordDetected()
        }
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification("Listening for wake word")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(notificationId, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(notificationId, notification)
        }
        
        if (repository.handsFreeEnabled.value) {
            startWakeWordListening()
        }
        
        return START_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Assistant Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(status: String): Notification {
        val stopIntent = Intent(this, AssistantService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val openIntent = Intent(this, MainActivity::class.java)
        val openPendingIntent = PendingIntent.getActivity(
            this, 0, openIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Voice Assistant")
            .setContentText(status)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }
    
    private fun startWakeWordListening() {
        repository.setState(AssistantState.WAKE_LISTENING)
        repository.setStatusText("Listening for wake word")
        updateNotification("Listening for wake word")
        wakeWordDetector.start()
    }
    
    private fun onWakeWordDetected() {
        serviceScope.launch {
            repository.playEarcon()
            ttsManager.stop() // Stop any ongoing TTS
            wakeWordDetector.stop()
            repository.setState(AssistantState.LISTENING)
            repository.setStatusText("Active - Listening")
            updateNotification("Active - Listening")
            
            // Listen for one command
            speechToText.startListening().collect { event ->
                when (event) {
                    is SpeechToText.SpeechEvent.FinalResult -> {
                        if (event.text.isNotBlank()) {
                            processCommand(event.text)
                        } else {
                            // Silence timeout
                            serviceScope.launch {
                                delay(6000)
                                if (repository.assistantState.value == AssistantState.LISTENING) {
                                    returnToWakeWordMode()
                                }
                            }
                        }
                    }
                    is SpeechToText.SpeechEvent.Error -> {
                        returnToWakeWordMode()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun processCommand(transcript: String) {
        serviceScope.launch {
            repository.setState(AssistantState.THINKING)
            repository.setStatusText("Thinking")
            updateNotification("Thinking")
            
            val commandResult = commandRouter.route(transcript)
            when (commandResult) {
                is CommandResult.Handled -> {
                    repository.setState(AssistantState.SPEAKING)
                    repository.setStatusText("Speaking")
                    updateNotification("Speaking")
                    ttsManager.speak(commandResult.spokenReply)
                    serviceScope.launch {
                        delay(5000) // Wait for speech
                        if (repository.followUpEnabled.value) {
                            // Follow-up window
                            startFollowUpListening()
                        } else {
                            returnToWakeWordMode()
                        }
                    }
                }
                is CommandResult.NotACommand -> {
                    val result = aiClient.getResponse(transcript)
                    result.fold(
                        onSuccess = { response ->
                            repository.setState(AssistantState.SPEAKING)
                            repository.setStatusText("Speaking")
                            updateNotification("Speaking")
                            ttsManager.speak(response)
                            serviceScope.launch {
                                delay(5000)
                                if (repository.followUpEnabled.value) {
                                    startFollowUpListening()
                                } else {
                                    returnToWakeWordMode()
                                }
                            }
                        },
                        onFailure = {
                            returnToWakeWordMode()
                        }
                    )
                }
            }
        }
    }
    
    private fun startFollowUpListening() {
        repository.setState(AssistantState.LISTENING)
        repository.setStatusText("Follow-up - Listening")
        updateNotification("Follow-up - Listening")
        serviceScope.launch {
            speechToText.startListening().collect { event ->
                when (event) {
                    is SpeechToText.SpeechEvent.FinalResult -> {
                        if (event.text.isNotBlank()) {
                            processCommand(event.text)
                        } else {
                            returnToWakeWordMode()
                        }
                    }
                    is SpeechToText.SpeechEvent.Error -> {
                        returnToWakeWordMode()
                    }
                    else -> {}
                }
            }
            delay(5000)
            if (repository.assistantState.value == AssistantState.LISTENING) {
                returnToWakeWordMode()
            }
        }
    }
    
    private fun returnToWakeWordMode() {
        speechToText.stopListening()
        if (repository.handsFreeEnabled.value) {
            startWakeWordListening()
        } else {
            repository.setState(AssistantState.IDLE)
            repository.setStatusText("Idle")
            updateNotification("Idle")
            stopSelf()
        }
    }
    
    private fun updateNotification(status: String) {
        val notification = createNotification(status)
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        wakeWordDetector.stop()
        speechToText.stopListening()
        ttsManager.release()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
