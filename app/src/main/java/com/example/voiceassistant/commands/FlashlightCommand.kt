package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Build

class FlashlightCommand(private val context: Context) : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.contains("flashlight") || transcript.contains("torch")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = cameraManager.cameraIdList[0]
            if (transcript.contains("on")) {
                cameraManager.setTorchMode(cameraId, true)
                return CommandResult.Handled("Flashlight on")
            } else if (transcript.contains("off")) {
                cameraManager.setTorchMode(cameraId, false)
                return CommandResult.Handled("Flashlight off")
            }
        } catch (e: Exception) {
            return CommandResult.Handled("Flashlight not available")
        }
        return CommandResult.Handled("Say flashlight on or off")
    }
}
