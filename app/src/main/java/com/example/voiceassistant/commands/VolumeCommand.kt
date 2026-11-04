package com.example.voiceassistant

import android.content.Context
import android.media.AudioManager

class VolumeCommand(private val context: Context) : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.contains("volume")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        return when {
            transcript.contains("up") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                CommandResult.Handled("Volume up")
            }
            transcript.contains("down") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                CommandResult.Handled("Volume down")
            }
            transcript.contains("mute") -> {
                audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                CommandResult.Handled("Muted")
            }
            else -> CommandResult.Handled("Say volume up, down, or mute")
        }
    }
}
