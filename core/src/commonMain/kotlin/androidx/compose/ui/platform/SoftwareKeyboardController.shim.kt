package androidx.compose.ui.platform

// ==================
// MARK: SoftwareKeyboardController shim
// ==================

/**
 * Marker for upstream `SoftwareKeyboardController`. Desktop has no
 * on-screen soft keyboard.
 */
interface SoftwareKeyboardController {
	fun show() {}
	fun hide() {}
}

/** Marker for upstream `PlatformTextInputSessionScope`. */
interface PlatformTextInputSessionScope
