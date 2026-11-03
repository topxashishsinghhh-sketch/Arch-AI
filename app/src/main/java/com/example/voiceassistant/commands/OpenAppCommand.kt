package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class OpenAppCommand : Command {
    private val appMap = mapOf(
        "whatsapp" to "com.whatsapp",
        "youtube" to "com.google.android.youtube",
        "chrome" to "com.android.chrome"
    )
    
    override fun matches(transcript: String): Boolean {
        return transcript.contains("open ")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        for ((appName, packageName) in appMap) {
            if (transcript.contains(appName)) {
                val intent = context.packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return CommandResult.Handled("Opened $appName")
                } else {
                    return CommandResult.Handled("$appName is not installed")
                }
            }
        }
        // Try fuzzy match on installed apps
        val pm = context.packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val query = transcript.substringAfter("open ").trim()
        
        for (app in installedApps) {
            val label = pm.getApplicationLabel(app).toString().lowercase()
            if (label.contains(query) || query.contains(label)) {
                val intent = pm.getLaunchIntentForPackage(app.packageName)
                if (intent != null) {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    return CommandResult.Handled("Opened ${pm.getApplicationLabel(app)}")
                }
            }
        }
        return CommandResult.Handled("App not found")
    }
}
