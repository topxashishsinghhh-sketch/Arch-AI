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
                // Add user message to history (Gemini uses "user" and "model")
                conversationHistory.add(ChatMessage("user", userMessage))
                
                // Trim history to last 10 turns (20 messages)
                if (conversationHistory.size > 20) {
                    val toRemove = conversationHistory.size - 20
                    repeat(toRemove) {
                        conversationHistory.removeAt(0)
                    }
                }
                
                val requestBody = buildRequestBody()
                val modelName = BuildConfig.MODEL_NAME
                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent")
                    .addHeader("x-goog-api-key", BuildConfig.GEMINI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody.toRequestBody(mediaType))
                    .build()
                
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withTimeout when (response.code) {
                        401, 403 -> Result.failure(IOException("Invalid API key"))
                        429 -> Result.failure(IOException("Rate limit exceeded"))
                        else -> Result.failure(IOException("API error: ${response.code}"))
                    }
                }
                
                val responseBody = response.body?.string() ?: ""
                val assistantMessage = parseResponse(responseBody)
                
                if (assistantMessage.isEmpty()) {
                    return@withTimeout Result.failure(IOException("Empty response"))
                }
                
                // Add assistant message to history (Gemini uses "model")
                conversationHistory.add(ChatMessage("model", assistantMessage))
                
                Result.success(assistantMessage)
            }
        } catch (e: TimeoutCancellationException) {
            Result.failure(e)
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private fun buildRequestBody(): String {
        val contents = conversationHistory.joinToString(",") { msg ->
            val role = if (msg.role == "user") "user" else "model"
            """{"role":"$role","parts":[{"text":"${escapeJson(msg.content)}"}]}"""
        }
        
        return """{
            "systemInstruction": {
                "parts": [{"text":"${escapeJson(systemPrompt)}"}]
            },
            "contents": [$contents]
        }"""
    }
    
    private fun parseResponse(responseBody: String): String {
        try {
            // Check for safety block or no candidates
            if (responseBody.contains("\"blockReason\"") || !responseBody.contains("\"candidates\"")) {
                return "I'm unable to respond to that request."
            }
            
            // Parse candidates[0].content.parts[0].text
            val candidatesIndex = responseBody.indexOf("\"candidates\"")
            if (candidatesIndex == -1) return "Sorry, I couldn't understand that."
            
            val textIndex = responseBody.indexOf("\"text\":\"", candidatesIndex)
            if (textIndex == -1) return "Sorry, I couldn't understand that."
            
            val start = textIndex + 8
            val end = responseBody.indexOf("\"", start)
            if (end == -1) return "Sorry, I couldn't understand that."
            
            val text = responseBody.substring(start, end)
                .replace("\\n", " ")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
            
            return text
        } catch (e: Exception) {
            return "Sorry, I couldn't understand that."
        }
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
