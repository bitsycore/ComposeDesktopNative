package androidx.compose.ui.text.input

// ==================
// MARK: TextInputService shim (deprecated upstream)
// ==================

/**
 * Upstream deprecated `TextInputService` — replaced by
 * `PlatformTextInputModifierNode`. Not wired on desktop.
 */
@Deprecated("Use PlatformTextInputModifierNode instead")
open class TextInputService(
	@Suppress("UNUSED_PARAMETER") platformTextInputService: PlatformTextInputService,
)

/** Marker for upstream `PlatformTextInputService`. */
@Deprecated("Use PlatformTextInputModifierNode instead")
interface PlatformTextInputService
