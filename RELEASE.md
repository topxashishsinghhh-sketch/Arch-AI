# RELEASE.md

## Pre-Launch Checklist

### Build & Configuration
- [ ] Update versionCode and versionName in app/build.gradle.kts
- [ ] Configure release signing in app/build.gradle.kts
- [ ] Test release build: `./gradlew assembleRelease`
- [ ] Verify ProGuard/R8 doesn't break Room, serialization, Porcupine
- [ ] Check targetSdk is latest stable

### Permissions & Privacy
- [ ] Review all permissions in AndroidManifest.xml
- [ ] Add permission justifications for Play Store
- [ ] Create privacy policy and host it
- [ ] Complete Data Safety form
- [ ] Review what data is collected and sent

### Features Testing
- [ ] Test all voice commands
- [ ] Test hands-free mode with screen off
- [ ] Test phone control with confirmations
- [ ] Test offline mode
- [ ] Test error handling

### UI/UX
- [ ] App icon and adaptive icon
- [ ] Splash screen
- [ ] All strings externalized
- [ ] Accessibility tested with TalkBack
- [ ] Dark mode support (if desired)

## Manual Test Matrix

### Devices
| Device | Android Version | Notes |
|--------|-----------------|-------|
| Pixel 6 | Android 14 | Primary test device |
| Samsung Galaxy S21 | Android 13 | Test manufacturer variations |
| OnePlus | Android 12 | Test different OEMs |

### Scenarios
| Test | Expected | Status |
|------|----------|--------|
| First launch onboarding | Shows privacy notice | ☐ |
| Grant permissions | All permissions handled | ☐ |
| Voice command (offline) | Local commands work | ☐ |
| Voice command (online) | AI responds | ☐ |
| Hands-free wake word | Activates on "Hey Assistant" | ☐ |
| Hands-free screen off | Works with service running | ☐ |
| Call command | Requests confirmation | ☐ |
| SMS command | Opens SMS app | ☐ |
| Flashlight | Toggles torch | ☐ |
| Low battery pause | Pauses hands-free below threshold | ☐ |
| Network failure | Falls back gracefully | ☐ |
| TTS fallback | Uses on-device if cloud fails | ☐ |
| History save/disable | Respects setting | ☐ |
| Delete all data | Clears everything | ☐ |

## Build Commands

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build (AAB)
```bash
./gradlew bundleRelease
```

## Versioning
- versionCode: Increment for each release
- versionName: Semantic versioning (MAJOR.MINOR.PATCH)
