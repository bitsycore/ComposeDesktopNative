package screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.platformClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ==================
// MARK: Clipboard screen
// ==================

/* Round-trips text through the OS clipboard via LocalClipboardManager (the
   in-composition handle) and platformClipboardManager (the same thing from
   non-@Composable code). Both are backed by the SDL3 system clipboard, so
   you can copy here and paste into another app and vice-versa. */
@Composable
internal fun ClipboardScreen() {
    val clipboard = LocalClipboardManager.current

    var draft by remember { mutableStateOf("Hello from ComposeDesktopNative!") }
    var pasted by remember { mutableStateOf<String?>(null) }
    var hasText by remember { mutableStateOf(clipboard.hasText()) }
    var note by remember { mutableStateOf<String?>(null) }

    fun refresh() { hasText = clipboard.hasText() }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenTitle(
            "Clipboard",
            "LocalClipboardManager (AnnotatedString) over the SDL3 system clipboard — copy here, paste anywhere.",
        )

        Section("Copy", "LocalClipboardManager.current.setText(AnnotatedString(...))") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    label = "Text to copy",
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = {
                    clipboard.setText(AnnotatedString(draft))
                    note = "Copied ${draft.length} char(s) to the system clipboard."
                    refresh()
                }) {
                    Text("Copy", color = MaterialTheme.colors.onPrimary)
                }
            }
        }

        Section("Paste", "LocalClipboardManager.current.getText()?.text — reads the live OS clipboard") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Button(onClick = { pasted = clipboard.getText()?.text; refresh() }) {
                        Text("Paste", color = MaterialTheme.colors.onPrimary)
                    }
                    OutlinedButton(onClick = { refresh() }) {
                        Text("hasText = $hasText", color = MaterialTheme.colors.primary)
                    }
                }
                Text(
                    pasted?.let { "Clipboard contents: \"$it\"" } ?: "(press Paste to read the clipboard)",
                    color = MaterialTheme.colors.onBackground,
                    fontSize = 14.sp,
                )
            }
        }

        Section(
            "Outside composition",
            "platformClipboardManager (and the lower-level currentClipboard) are globals — usable from plain " +
                "functions, event callbacks, or coroutines with no Composable in scope.",
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = {
                    // Calls a plain, non-@Composable function (defined below).
                    copyFromPlainFunction()
                    note = "Wrote to the clipboard from a non-Composable function."
                    refresh()
                }) {
                    Text("Copy via global (no Composable)", color = MaterialTheme.colors.onPrimary)
                }
                if (note != null) {
                    Text(note!!, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }
        }
    }
}

/* Demonstrates clipboard access with no Composable / CompositionLocal in
   scope — the same global the in-composition manager delegates to. */
private fun copyFromPlainFunction() {
    platformClipboardManager.setText(AnnotatedString("Set from a plain, non-@Composable function."))
}
