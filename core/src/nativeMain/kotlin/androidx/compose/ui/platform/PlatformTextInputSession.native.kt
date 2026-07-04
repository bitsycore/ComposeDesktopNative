package androidx.compose.ui.platform

// ==================
// MARK: PlatformTextInputSession — native actual (marker)
// ==================

/*
 Actual for the `expect interface PlatformTextInputSession` in vendored
 `PlatformTextInputModifierNode.kt`. On desktop we don't have a
 platform text-input session yet (no keyboard/IME wiring), so the
 interface has just the upstream-shape `startInputMethod` method that
 hangs forever if ever called.
 TODO: wire real IME session when SDL3 text-input events + soft keyboard land.
*/
actual interface PlatformTextInputSession {
	actual suspend fun startInputMethod(request: PlatformTextInputMethodRequest): Nothing
}
