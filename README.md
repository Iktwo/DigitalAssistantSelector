# DAS — Digital Assistant Selector

An Android library that turns any app into a **digital assistant with on-screen
text selection**. Once the host app is configured as the device's default
assistant, DAS overlays the current screen with tappable bounding boxes around
every detected text element, lets the user pick the text they want, and hands
the selection back to the host app.

The implementation was extracted from [Mockery](https://github.com/iktwo/Mockery)
and litertlm, which both shipped near-identical copies of this mechanism.

## What it does

Adding the library to an app automatically merges the following into the app's
manifest:

- `DasVoiceInteractionService` — a `VoiceInteractionService` declared with
  `android:supportsAssist="true"`, which makes the system offer the app as a
  digital assistant candidate.
- `DasAssistSessionService` / `DasAssistSession` — the
  `VoiceInteractionSession` that renders a Compose overlay on top of the
  current screen: highlighted, tappable text regions, Select All / Deselect
  All, a preview of the selection, and a confirmation action.
- `DasRecognitionService` — a stub `RecognitionService`. Some OEMs (notably
  Samsung) require it for the "Analyze on-screen text", "Analyze on-screen
  images", and "Flash screen" assist options to appear in system settings.
- `DasAssistActivity` — fallback activity handling `ACTION_ASSIST` /
  `ACTION_VOICE_ASSIST` / `ACTION_SEARCH_LONG_PRESS` (forwards to the app's
  launcher activity).

It also provides `DasAssistant`, a small API to check whether the app is the
configured assistant, request the assistant role with the system dialog, or
send the user to the relevant settings screen.

## Integration

### 1. Add the dependency

```kotlin
implementation("com.iktwo:das:0.1.0")
```### 2. Configure DAS in your `Application`

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Das.init(
            DasConfig(
                title = "My Assistant",
                actionLabel = "Use Text",
                copyToClipboard = true,
                // Optional: handle the confirmed selection yourself.
                // Default behavior: copy to clipboard (if enabled) and forward
                // the text to the launcher activity as ACTION_PROCESS_TEXT.
                onTextSelected = { context, text -> /* ... */ },
                // Optional theming of the overlay.
                theme = DasOverlayTheme(
                    highlightColor = Color(0xFF8FF7F7),
                    selectedColor = Color(0xFFFFCC00)
                )
            )
        )
    }
}
```

That is the whole setup — no manifest entries are needed, everything merges
from the library.

### 3. Guide the user into enabling the assistant

```kotlin
if (!DasAssistant.isDefaultAssistant(context)) {
    // Preferred: system role request dialog (API 29+)
    val intent = DasAssistant.createRoleRequestIntent(context)
    if (intent != null) {
        startActivityForResult(intent, REQUEST_ASSISTANT_ROLE)
    } else {
        // Fallback: open the assistant settings screen
        DasAssistant.openAssistantSettings(context)
    }
}
```

Available API on `DasAssistant`:

| Function | Description |
|---|---|
| `isDefaultAssistant(context)` | True when the app is the current default assistant (RoleManager, with legacy `Settings.Secure` fallback). |
| `canRequestRole(context)` | True when the role can be requested via the system dialog. |
| `createRoleRequestIntent(context)` | Intent for `startActivityForResult`; `RESULT_OK` means the user accepted. |
| `openAssistantSettings(context)` | Opens the system voice-input/assistant settings screen. |

### Receiving the selection

With the default configuration, DAS copies the selected text to the clipboard
and launches the app's launcher activity with `ACTION_PROCESS_TEXT` +
`EXTRA_PROCESS_TEXT` — the same channel used by the standard Android text
selection menu, so most apps can reuse their existing handling:

```kotlin
if (intent.action == Intent.ACTION_PROCESS_TEXT) {
    val text = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
}
```

Alternatively pass `onTextSelected` in `DasConfig` for full control.

### Customizing `settingsActivity`

The system settings entry for the assistant can deep-link into an app screen.
To enable it, override the library resource `res/xml/das_voice_interaction_service.xml`
in your app module (same file name wins) and add the attribute:

```xml
<voice-interaction-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:sessionService="com.iktwo.das.DasAssistSessionService"
    android:recognitionService="com.iktwo.das.DasRecognitionService"
    android:settingsActivity="com.example.myapp.MainActivity"
    android:supportsAssist="true"
    android:supportsLaunchVoiceAssistFromKeyguard="true"
    android:supportsLocalInteraction="true" />
```

## Requirements

- Android API 26+ (role request dialog requires API 29+)
- Jetpack Compose (the overlay is Compose-based)

## Building

```bash
./gradlew :das:assembleRelease       # build the AAR
./gradlew :das:publishToMavenLocal   # install locally for testing
```

## Publishing

Publishing to Maven Central is handled by the
[vanniktech Maven Publish plugin](https://github.com/vanniktech/gradle-maven-publish-plugin)
via `.github/workflows/publish.yml`, which runs on GitHub releases.

Credentials are **never stored in the repository** — they are injected at CI
time as environment variables from GitHub Actions secrets:

| Secret | Purpose |
|---|---|
| `MAVEN_CENTRAL_USERNAME` / `MAVEN_CENTRAL_PASSWORD` | Sonatype Central Portal user token |
| `SIGNING_KEY_ID` / `SIGNING_PASSWORD` / `GPG_KEY_CONTENTS` | In-memory GPG key used to sign publications |

To publish: create a GitHub release (the workflow triggers on `released` and
`prereleased`) with the required secrets configured in the repo settings.
