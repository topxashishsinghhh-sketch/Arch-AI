package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock

class AlarmTimerCommand : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.contains("alarm") || transcript.contains("timer")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        return when {
            transcript.contains("alarm") -> {
                // Simple parsing for time
                val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Alarm")
                    putExtra(AlarmClock.EXTRA_HOUR, 7)
                    putExtra(AlarmClock.EXTRA_MINUTES, 0)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult.Handled("Setting alarm")
            }
            transcript.contains("timer") -> {
                val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                    putExtra(AlarmClock.EXTRA_MESSAGE, "Timer")
                    putExtra(AlarmClock.EXTRA_LENGTH, 600) // 10 minutes
                    putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult.Handled("Setting timer")
            }
            else -> CommandResult.NotACommand
        }
    }
}
