package androidx.compose.foundation.text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

// ==================
// MARK: ContextMenuStrings — native actual (id + hardcoded English)
// ==================

@kotlin.jvm.JvmInline
internal actual value class ContextMenuStrings actual constructor(actual val value: Int) {
	actual companion object {
		actual val Cut: ContextMenuStrings = ContextMenuStrings(0)
		actual val Copy: ContextMenuStrings = ContextMenuStrings(1)
		actual val Paste: ContextMenuStrings = ContextMenuStrings(2)
		actual val SelectAll: ContextMenuStrings = ContextMenuStrings(3)
		actual val Autofill: ContextMenuStrings = ContextMenuStrings(4)
	}
}

/** Desktop path — no localization yet; hardcoded English. */
@Composable
@ReadOnlyComposable
internal actual fun getString(string: ContextMenuStrings): String = when (string.value) {
	0 -> "Cut"
	1 -> "Copy"
	2 -> "Paste"
	3 -> "Select all"
	4 -> "Autofill"
	else -> ""
}
