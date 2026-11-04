package com.example.voiceassistant

import org.junit.Test
import org.junit.Assert.*

class CommandRouterTest {
    
    @Test
    fun testOpenAppCommand() {
        // Test would require Android context, but we can test parsing logic
        val router = CommandRouter(android.app.Application())
        // This is a basic structure test
        assertNotNull(router)
    }
    
    @Test
    fun testTimeCommand() {
        val cmd = TimeDateCommand()
        assertTrue(cmd.matches("what time is it"))
        assertTrue(cmd.matches("tell me the time"))
    }
    
    @Test
    fun testDateCommand() {
        val cmd = TimeDateCommand()
        assertTrue(cmd.matches("what is today's date"))
    }
    
    @Test
    fun testWebSearchCommand() {
        val cmd = WebSearchCommand()
        assertTrue(cmd.matches("search for android tutorials"))
        assertFalse(cmd.matches("look up something"))
    }
    
    @Test
    fun testFlashlightCommand() {
        val cmd = FlashlightCommand(android.app.Application())
        assertTrue(cmd.matches("turn on flashlight"))
        assertTrue(cmd.matches("torch off"))
    }
    
    @Test
    fun testVolumeCommand() {
        val cmd = VolumeCommand(android.app.Application())
        assertTrue(cmd.matches("volume up"))
        assertTrue(cmd.matches("turn volume down"))
    }
    
    @Test
    fun testSettingsCommand() {
        val cmd = SettingsCommand()
        assertTrue(cmd.matches("open wifi settings"))
        assertTrue(cmd.matches("bluetooth settings"))
    }
}
