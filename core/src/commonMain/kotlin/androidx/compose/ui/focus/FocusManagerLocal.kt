package androidx.compose.ui.focus

import androidx.compose.runtime.staticCompositionLocalOf

// ==================
// MARK: LocalFocusManager
// ==================

/*
 `LocalFocusManager.current` — upstream places this in androidx.compose.ui.platform; we keep it in
 the focus package to match existing call sites. The window layer provides the real one (the
 ComposeOwner's FocusOwner, which IS a FocusManager). The default is a no-op so a composition run
 off a window (probes/tests) still resolves it.
*/
val LocalFocusManager = staticCompositionLocalOf<FocusManager> {
	object : FocusManager {
		override fun clearFocus(force: Boolean) {}
		override fun moveFocus(focusDirection: FocusDirection): Boolean = false
	}
}
