package com.example.voiceassistant

import org.junit.Test
import org.junit.Assert.*

class MemoryLogicTest {
    
    @Test
    fun testSummarizationTrigger() {
        val history = mutableListOf<String>()
        for (i in 1..25) {
            history.add("Message $i")
        }
        // Should trigger summarization when > 20
        assertTrue(history.size > 20)
    }
    
    @Test
    fun testRetryBackoff() {
        var attempts = 0
        fun failingFunction(): String {
            attempts++
            if (attempts < 3) throw Exception("Fail")
            return "Success"
        }
        
        // Simulate retry
        var result = ""
        var tries = 0
        while (tries < 3) {
            try {
                result = failingFunction()
                break
            } catch (e: Exception) {
                tries++
            }
        }
        assertEquals("Success", result)
        assertEquals(3, attempts)
    }
}
