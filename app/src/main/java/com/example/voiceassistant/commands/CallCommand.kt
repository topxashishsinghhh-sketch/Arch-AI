package com.example.voiceassistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract

class CallCommand(private val context: Context) : Command {
    override fun matches(transcript: String): Boolean {
        return transcript.startsWith("call ")
    }
    
    override suspend fun execute(transcript: String, context: Context): CommandResult {
        val query = transcript.substringAfter("call ").trim()
        // Check if it's a number
        if (query.matches(Regex("^[0-9+\\s-()]+$"))) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$query")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult.Handled("Calling $query")
        }
        
        // Look up contact
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$query%"),
            null
        )
        
        val contacts = mutableListOf<Pair<String, String>>()
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                contacts.add(name to number)
            }
        }
        
        return when {
            contacts.isEmpty() -> CommandResult.Handled("Contact not found")
            contacts.size == 1 -> {
                val (name, number) = contacts[0]
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$number")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                CommandResult.Handled("Calling $name")
            }
            else -> {
                // Multiple matches - ask to choose
                val names = contacts.take(3).joinToString(", ") { it.first }
                CommandResult.Handled("Multiple matches found: $names. Which one do you want to call?")
            }
        }
    }
}
