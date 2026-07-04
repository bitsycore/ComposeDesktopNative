package androidx.compose.foundation.text

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint

// ==================
// MARK: isTypedEvent — extract
// ==================

/*
 Byte-identical extract of upstream `foundation.text.TextFieldKeyInput.macos.kt`.
 The `internal expect val KeyEvent.isTypedEvent: Boolean` is declared inside
 the unvendored `TextFieldKeyInput.kt` (blocked on
 `input.internal.selection.TextPreparedSelection` — the BasicTextField-2 file
 we couldn't vendor yet). Vendored TextFieldKeyEventHandler.kt reads
 isTypedEvent, so we ship the desktop-shape val here as a project extract.

 TODO: delete when TextFieldKeyInput.kt can vendor.
*/
internal val KeyEvent.isTypedEvent: Boolean
	get() = type == KeyEventType.KeyDown &&
		!isISOControl(utf16CodePoint) &&
		!isAppKitReserved(utf16CodePoint) &&
		!isMetaPressed &&
		!isCtrlPressed

private fun isISOControl(codePoint: Int): Boolean =
	codePoint in 0x00..0x1F ||
	codePoint in 0x7F..0x9F

private fun isAppKitReserved(codePoint: Int): Boolean =
	codePoint in 0xF700..0xF8FF
