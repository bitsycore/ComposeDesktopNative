package com.compose.desktop.native.input

// ==================
// MARK: KeyModifiers (project-only render-bridge)
// ==================

/* Project-only data class for the four standard keyboard modifier bits.
   Upstream Compose has no `KeyModifiers` type — it encodes the modifier
   state into the `KeyEvent` itself and exposes extension properties
   like `event.isCtrlPressed`. Per FIDELITY relocate rule, lives here
   instead of in androidx.compose.ui.input.key.

   The full upstream redesign (KeyEvent as value class + extension props)
   is FIDELITY-flagged as runtime-critical and deferred to its own pass —
   in the meantime KeyEvent.modifiers stays a typed field reading this
   data class. */
data class KeyModifiers(
	val shift: Boolean = false,
	val ctrl: Boolean = false,
	val alt: Boolean = false,
	val meta: Boolean = false,
)
