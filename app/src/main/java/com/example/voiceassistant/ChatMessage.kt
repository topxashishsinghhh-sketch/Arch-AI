package com.example.voiceassistant

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val isUser: Boolean,
    val isAction: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

