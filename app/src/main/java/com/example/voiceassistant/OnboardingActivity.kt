package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OnboardingScreen {
                        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        prefs.edit().putBoolean("onboarding_done", true).apply()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Welcome to Arch",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "Privacy & Data Policy",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "What We Collect:\n• Voice input (processed in real-time, never stored as audio)\n• Transcripts (only if conversation history is enabled)\n• Commands executed on your device\n\nWhat We Don't Collect:\n• We do NOT store your voice recordings\n• We do NOT use tracking cookies or third-party analytics\n• We do NOT sell your personal data\n• We do NOT share your conversations with advertisers\n\nData Processing:\n• Microphone access is used solely for voice commands\n• AI queries are sent to the configured API provider only when needed\n• All on-device commands (calls, apps, alarms, etc.) never leave your device\n• API keys are stored locally and never transmitted to our servers\n\nYour Rights:\n• Disable history anytime in Settings\n• Delete all data with one tap\n• Revoke permissions at any system level\n• Use offline commands without any data transmission",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(onClick = onComplete) {
            Text("I Understand & Agree")
        }
    }
}
