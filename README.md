# Voice Assistant

An Android voice assistant app built with Kotlin and Jetpack Compose. 
- Milestone 1: Voice input → Speech-to-text → AI understanding → Spoken response.
- Milestone 2: Phone control features with CommandRouter.
- Milestone 3: Wake-word detection and hands-free operation.

## Features
- Speech recognition using Android's SpeechRecognizer
- AI responses via OpenAI API (fallback)
- Text-to-speech for spoken responses
- Jetpack Compose UI with mic button and chat history
- Runtime permission handling
- Command routing for phone control
- Voice confirmation for sensitive actions
- Settings screen
- Foreground service for hands-free mode
- Wake-word detection (Porcupine/Picovoice)
- Audio handoff and focus management

## Phone Control Commands
- "Open WhatsApp/YouTube/Chrome" - Launch apps
- "Call John" or "Call 1234567890" - Dial contacts/numbers (ACTION_DIAL)
- "Send a message to John" - Open SMS app
- "Set an alarm for 7 AM" / "Set a timer for 10 minutes" - Set alarms/timers
- "Turn flashlight on/off" - Control flashlight
- "Open Wi-Fi/Bluetooth/Settings" - Open settings panels
- "Volume up/down/mute" - Adjust volume
- "What time is it?" / "What's the date?" - Local time/date
- "Search for Android tutorials" - Open browser search

## Setup

### Prerequisites
- Android Studio
- Android SDK with min SDK 26
- OpenAI API key
- Porcupine/Picovoice access key (for hands-free mode)

### Configuration
1. Create a `local.properties` file in the root of the project (same level as `build.gradle.kts`)
2. Add your API keys and model:
```properties
API_KEY=your_openai_api_key_here
MODEL_NAME=gpt-3.5-turbo
PORCUPINE_ACCESS_KEY=your_picovoice_access_key_here
```
3. Sync the project with Gradle files

### Getting Porcupine Access Key
1. Go to [Picovoice Console](https://console.picovoice.ai/)
2. Create an account and get a free access key
3. Add it to local.properties as PORCUPINE_ACCESS_KEY
4. For custom wake words, drop .ppn files in assets folder

### Running
1. Open the project in Android Studio
2. Connect a physical Android device or start an emulator
3. Run the app

## Architecture
- **MainActivity**: Single activity, handles permissions, navigation
- **VoiceViewModel**: MVVM orchestrator, observes service state
- **AssistantService**: Foreground service managing hands-free pipeline
- **AssistantRepository**: Shared state between service and UI
- **CommandRouter**: Routes commands to appropriate handlers
- **WakeWordDetector**: Interface for wake word detection
- **SpeechToText**: Wraps Android SpeechRecognizer with Flow API
- **AiClient**: Handles API calls to LLM with conversation history (fallback)
- **TextToSpeechManager**: Wraps Android TextToSpeech

## Hands-Free Mode
1. Grant all permissions (microphone, notifications)
2. Go to Settings and enable "Hands-free mode"
3. Disable battery optimization for better reliability
4. Say "Hey Assistant" to activate
5. Speak your command
6. Assistant will respond and return to wake-word listening

## Safety
- Sensitive actions (calls, messages) require voice confirmation
- Direct calling (ACTION_CALL) is disabled by default in settings
- Permissions requested only when needed
- All actions speak feedback before/after execution
- Foreground service shows persistent notification
- Audio focus handled for calls

## Testing

### Unit Tests
```bash
./gradlew test
```

### Manual Test Phrases
**Commands:**
- "Open YouTube" - Should launch YouTube
- "What time is it?" - Should speak current time
- "What's the date?" - Should speak current date
- "Search for weather" - Should open browser
- "Volume up" - Should increase volume
- "Turn flashlight on" - Should turn on torch
- "Open Wi-Fi settings" - Should open Wi-Fi settings
- "Call 1234567890" - Should open dialer (needs confirmation)
- "Call Mom" - Should look up contact and ask for confirmation
- "Send a message to John" - Should open SMS app
- "Set a timer for 5 minutes" - Should open timer app
- "Tell me a joke" - Should go to AI (not a command)

**Hands-Free:**
1. Enable hands-free mode with screen on
2. Say "Hey Assistant" - should play earcon and start listening
3. Give a command - should process and return to wake-word mode
4. Test with screen off
5. Test with music playing (audio focus)
6. Simulate incoming call
7. Stop service and restart app
8. Test with denied permissions

## Notes
- Use a physical device for testing (emulator mics unreliable)
- Some features require specific permissions
- Flashlight requires camera permission
- Contacts require READ_CONTACTS permission
- Porcupine requires internet for activation but works offline after
- Disable battery optimization for reliable background operation

## Out of Scope
- Accessibility-service automation
- Custom wake-word training UI
- Running before first app launch


