package androidx.compose.ui.input.key

import com.compose.desktop.native.input.KeyModifiers

// ==================
// MARK: Key Events
// ==================

// Project-only reduced shape — FIDELITY-flagged runtime-critical for the
// value-class + extension-props redesign upstream uses. `modifiers` reads
// the relocated KeyModifiers in com.compose.desktop.native.input.
data class KeyEvent(
    val keyCode: Int,
    val char: Char?,
    val type: KeyEventType,
    val modifiers: KeyModifiers = KeyModifiers()
)

enum class KeyEventType { Down, Up }
