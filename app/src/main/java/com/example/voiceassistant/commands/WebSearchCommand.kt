package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.net.Uri

class WebSearchCommand : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.startsWith("search for ")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        val query = transcript.substringAfter("search for ").trim()
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return CommandResult.Handled("Searching for $query")
    }
}
