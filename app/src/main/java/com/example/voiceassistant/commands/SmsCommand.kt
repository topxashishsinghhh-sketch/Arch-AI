package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.net.Uri

class SmsCommand : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.contains("send a message to") || transcript.contains("text ")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        val name = if (transcript.contains("send a message to")) {
            transcript.substringAfter("send a message to").trim()
        } else {
            transcript.substringAfter("text ").trim()
        }
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return CommandResult.Handled("Opening messages to $name")
    }
}
