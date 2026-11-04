package com.example.voiceassistant

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

class AiClient {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val json = Json { ignoreUnknownKeys = true }
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    
    // Keep last ~10 turns
    private val conversationHistory = mutableListOf<ChatMessage>()
    
    private val systemPrompt = "You are a concise voice assistant. Reply in 1-3 short sentences suitable for speaking aloud."
    
    data class ChatMessage(
        val role: String,
        val content: String
    )
    
    suspend fun getResponse(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            withTimeout(30000L) {
                // Add user message to history
                conversationHistory.add(ChatMessage("user", userMessage))
                
                // Trim history to last 10 turns (20 messages)
                if (conversationHistory.size > 20) {
                    val toRemove = conversationHistory.size - 20
                    repeat(toRemove) {
                        conversationHistory.removeAt(0)
                    }
                }
                
                val messages = mutableListOf(
                    ChatMessage("system", systemPrompt)
                )
                messages.addAll(conversationHistory)
                
                val requestBody = buildRequestBody(messages)
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer ${BuildConfig.API_KEY}")
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody(mediaType))
                    .build()
                
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withTimeout Result.failure(IOException("API error: ${response.code}"))
                }
                
                val responseBody = response.body?.string() ?: ""
                val assistantMessage = parseResponse(responseBody)
                
                // Add assistant message to history
                conversationHistory.add(ChatMessage("assistant", assistantMessage))
                
                Result.success(assistantMessage)
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildRequestBody(messages: List<ChatMessage>): String {
        val messagesJson = messages.joinToString(",") { msg ->
            """{"role":"${msg.role}","content":"${escapeJson(msg.content)}"}"""
        }
        return """{"model":"${BuildConfig.MODEL_NAME}","messages":[$messagesJson],"temperature":0.7,"max_tokens":150}"""
    }
    
    private fun parseResponse(responseBody: String): String {
        // Simple parsing - in production use proper JSON parsing
        val contentStart = responseBody.indexOf("\"content\":\"")
        if (contentStart == -1) return "Sorry, I couldn't understand that."
        val start = contentStart + 11
        val end = responseBody.indexOf("\"", start)
        if (end == -1) return "Sorry, I couldn't understand that."
        return responseBody.substring(start, end)
            .replace("\\n", " ")
            .replace("\\\"", "\"")
    }
    
    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
    
    fun clearHistory() {
        conversationHistory.clear()
    }
}
