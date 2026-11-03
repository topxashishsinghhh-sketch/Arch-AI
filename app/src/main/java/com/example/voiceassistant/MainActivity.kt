package com.example.voiceassistant

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {
    
    private val viewModel: VoiceViewModel by viewModels()
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions gracefully
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val permissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.CAMERA
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())
        
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    VoiceAssistantApp(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantApp(viewModel: VoiceViewModel) {
    var showSettings by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Assistant") },
                actions = {
                    Button(onClick = { showSettings = !showSettings }) {
                        Text(if (showSettings) "Chat" else "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (showSettings) {
            SettingsScreen(viewModel, Modifier.padding(paddingValues))
        } else {
            VoiceAssistantScreen(viewModel, Modifier.padding(paddingValues))
        }
    }
}

@Composable
fun SettingsScreen(viewModel: VoiceViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val directCallingEnabled by viewModel.directCallingEnabled.collectAsState()
    val handsFreeEnabled by viewModel.handsFreeEnabled.collectAsState()
    val followUpEnabled by viewModel.followUpEnabled.collectAsState()
    val earconEnabled by viewModel.earconEnabled.collectAsState()
    var sensitivity by remember { mutableStateOf(0.5f) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        Text(
            text = "Hands-Free",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hands-free mode (wake word)")
            Switch(
                checked = handsFreeEnabled,
                onCheckedChange = { viewModel.setHandsFreeEnabled(it, context) }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Follow-up window")
            Switch(
                checked = followUpEnabled,
                onCheckedChange = { viewModel.setFollowUpEnabled(it) }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Earcon on wake word")
            Switch(
                checked = earconEnabled,
                onCheckedChange = { viewModel.setEarconEnabled(it) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Wake word sensitivity")
        Slider(
            value = sensitivity,
            onValueChange = { sensitivity = it },
            valueRange = 0f..1f,
            modifier = Modifier.fillMaxWidth()
        )
        
        Text(
            text = "Privacy & Security",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Button(
            onClick = {
                // Delete all data
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Delete All My Data")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Clears conversations, settings, and cached data",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Direct Calling (ACTION_CALL)")
            Switch(
                checked = directCallingEnabled,
                onCheckedChange = { viewModel.setDirectCalling(it) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Battery Optimization",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Disable Battery Optimization")
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "For better hands-free reliability, disable battery optimization",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceAssistantScreen(viewModel: VoiceViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val statusText by viewModel.statusText.collectAsState()
    val confirmationState by viewModel.confirmationState.collectAsState()
    val handsFreeEnabled by viewModel.handsFreeEnabled.collectAsState()
    val assistantState by viewModel.assistantState.collectAsState()
    val listState = rememberLazyListState()
    
    val audioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hands-free mode")
            Switch(
                checked = handsFreeEnabled,
                onCheckedChange = { viewModel.setHandsFreeEnabled(it, context) }
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (handsFreeEnabled) "Service: ${assistantState.name}" else statusText,
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                handsFreeEnabled && assistantState.name == "LISTENING" -> Color.Red
                handsFreeEnabled && assistantState.name == "THINKING" -> Color.Blue
                handsFreeEnabled && assistantState.name == "SPEAKING" -> Color.Green
                handsFreeEnabled && assistantState.name == "WAKE_LISTENING" -> Color.Orange
                uiState is VoiceViewModel.UiState.Listening -> Color.Red
                uiState is VoiceViewModel.UiState.Thinking -> Color.Blue
                uiState is VoiceViewModel.UiState.Speaking -> Color.Green
                else -> Color.Gray
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (confirmationState is VoiceViewModel.ConfirmationState.Pending) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Yellow.copy(alpha = 0.3f))
            ) {
                Text(
                    text = "⚠️ Confirmation required - say yes or no",
                    modifier = Modifier.padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
        }
        
        if (!audioPermissionState.status.isGranted) {
            Text(
                text = "Microphone permission required",
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Button(
                onClick = { audioPermissionState.launchPermissionRequest() }
            ) {
                Text("Grant Permission")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        if (!handsFreeEnabled) {
            Button(
                onClick = { 
                    if (audioPermissionState.status.isGranted) {
                        viewModel.onMicPressed()
                    } else {
                        audioPermissionState.launchPermissionRequest()
                    }
                },
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                enabled = audioPermissionState.status.isGranted && uiState !is VoiceViewModel.UiState.Thinking
            ) {
                Text(
                    text = when (uiState) {
                        is VoiceViewModel.UiState.Listening -> "⏹"
                        is VoiceViewModel.UiState.Speaking -> "⏹"
                        else -> "🎤"
                    },
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = when (uiState) {
                    is VoiceViewModel.UiState.Idle -> if (confirmationState is VoiceViewModel.ConfirmationState.Pending) "Waiting for confirmation..." else "Tap mic to speak"
                    is VoiceViewModel.UiState.Listening -> "Listening... Tap to stop"
                    is VoiceViewModel.UiState.Thinking -> "Thinking..."
                    is VoiceViewModel.UiState.Speaking -> "Speaking... Tap to stop"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        } else {
            Text(
                text = "Say 'Hey Assistant' to activate",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(0.8f),
            shape = if (message.isAction) RoundedCornerShape(16.dp) else RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    message.isUser -> MaterialTheme.colorScheme.primary
                    message.isAction -> MaterialTheme.colorScheme.tertiaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            )
        ) {
            Text(
                text = if (message.isAction) "⚡ ${message.text}" else message.text,
                modifier = Modifier.padding(12.dp),
                color = when {
                    message.isUser -> MaterialTheme.colorScheme.onPrimary
                    message.isAction -> MaterialTheme.colorScheme.onTertiaryContainer
                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                }
            )
        }
    }
}
