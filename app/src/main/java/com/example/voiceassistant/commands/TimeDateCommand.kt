package com.example.voiceassistant

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimeDateCommand : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.contains("time") || transcript.contains("date")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        return when {
            transcript.contains("time") && !transcript.contains("date") -> {
                val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
                CommandResult.Handled("The time is $time")
            }
            transcript.contains("date") -> {
                val date = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
                CommandResult.Handled("Today is $date")
            }
            else -> CommandResult.NotACommand
        }
    }
}
