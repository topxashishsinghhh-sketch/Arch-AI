package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.provider.Settings

class SettingsCommand : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.contains("wifi") || transcript.contains("bluetooth") || 
               transcript.contains("settings")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        val intent = when {
            transcript.contains("wifi") -> Intent(Settings.ACTION_WIFI_SETTINGS)
            transcript.contains("bluetooth") -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return CommandResult.Handled("Opening settings")
    }
}
