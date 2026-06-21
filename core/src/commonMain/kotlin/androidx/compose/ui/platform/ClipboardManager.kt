package androidx.compose.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.AnnotatedString

// ==================
// MARK: ClipboardManager
// ==================

/* The Compose-idiomatic clipboard handle (AnnotatedString in / out), matching
   androidx.compose.ui.platform.ClipboardManager. It's a thin wrapper over the
   lower-level platform `Clipboard` (currentClipboard), which the native backend
   wires to SDL3 during composeWindow startup.

   Reach it from composition via LocalClipboardManager.current, or from
   non-@Composable code (event handlers, coroutines, plain functions) via the
   global `platformClipboardManager`. */
interface ClipboardManager {
    fun setText(annotatedString: AnnotatedString)
    fun getText(): AnnotatedString?
    fun hasText(): Boolean = getText() != null
}

/* Default manager: forwards to the platform Clipboard global so it always
   reflects whatever backend composeWindow installed (SDL3 on every target
   here). OS clipboards exchange plain text, so AnnotatedString styling is
   dropped on copy and reads come back as unstyled text. */
private object PlatformClipboardManager : ClipboardManager {
    override fun setText(annotatedString: AnnotatedString) {
        currentClipboard.setText(annotatedString.text)
    }

    override fun getText(): AnnotatedString? =
        currentClipboard.getText()?.let { AnnotatedString(it) }

    override fun hasText(): Boolean = !currentClipboard.getText().isNullOrEmpty()
}

/* Clipboard access OUTSIDE composition. Always tracks the current platform
   Clipboard. Inside composition prefer LocalClipboardManager.current. */
val platformClipboardManager: ClipboardManager get() = PlatformClipboardManager

/* In-composition accessor — `LocalClipboardManager.current`. Defaults to the
   platform manager, so it resolves without an explicit CompositionLocalProvider. */
val LocalClipboardManager = staticCompositionLocalOf<ClipboardManager> { PlatformClipboardManager }
