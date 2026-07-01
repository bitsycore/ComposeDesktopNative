package androidx.compose.ui.focus

// ==================
// MARK: FocusOwner shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.focus.FocusOwner`. Real
 * interface has 30+ members (moveFocus/clearFocus/getFocusRect/etc.);
 * we expose only the bare shape vendored `Owner.kt` needs.
 */
interface FocusOwner {
	fun clearFocus(force: Boolean = false, refreshFocusEvents: Boolean = true) {}
}
