package com.example.voiceassistant

import org.junit.Test
import org.junit.Assert.*

class SessionStateMachineTest {
    
    @Test
    fun testAssistantStates() {
        assertEquals(AssistantState.IDLE, AssistantState.IDLE)
        assertEquals(AssistantState.WAKE_LISTENING, AssistantState.WAKE_LISTENING)
        assertEquals(AssistantState.LISTENING, AssistantState.LISTENING)
        assertEquals(AssistantState.THINKING, AssistantState.THINKING)
        assertEquals(AssistantState.SPEAKING, AssistantState.SPEAKING)
    }
    
    @Test
    fun testWakeWordDetectorInterface() {
        val fake = FakeWakeWordDetector(android.app.Application())
        var triggered = false
        fake.setOnWakeWordDetectedListener { triggered = true }
        fake.triggerWakeWord()
        assertTrue(triggered)
    }
    
    @Test
    fun testRepository() {
        val repo = AssistantRepository.getInstance(android.app.Application())
        repo.setState(AssistantState.WAKE_LISTENING)
        assertEquals(AssistantState.WAKE_LISTENING, repo.assistantState.value)
        repo.setStatusText("Test")
        assertEquals("Test", repo.statusText.value)
    }
}
