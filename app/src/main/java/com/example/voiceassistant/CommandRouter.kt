package com.example.voiceassistant

sealed class CommandResult {
    data class Handled(val spokenReply: String) : CommandResult()
    object NotACommand : CommandResult()
}

interface Command {
    fun matches(transcript: String): Boolean
    suspend fun execute(transcript: String, context: android.content.Context): CommandResult
}

class CommandRouter(private val context: android.content.Context) {
    private val commands = mutableListOf<Command>()
    
    init {
        // Register commands
        commands.add(OpenAppCommand())
        commands.add(CallCommand(context))
        commands.add(SmsCommand())
        commands.add(AlarmTimerCommand())
        commands.add(FlashlightCommand(context))
        commands.add(SettingsCommand())
        commands.add(VolumeCommand(context))
        commands.add(TimeDateCommand())
        commands.add(WebSearchCommand())
    }
    
    suspend fun route(transcript: String): CommandResult {
        val normalized = transcript.lowercase().trim()
        for (command in commands) {
            if (command.matches(normalized)) {
                return command.execute(transcript, context)
            }
        }
        return CommandResult.NotACommand
    }
    
    fun addCommand(command: Command) {
        commands.add(0, command) // Add to front for priority
    }
}
