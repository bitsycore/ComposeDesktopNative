package androidx.compose.ui.input.pointer

// ==================
// MARK: PointerIcon(Service) shim
// ==================

/** Upstream `PointerIcon` — opaque cursor descriptor. */
interface PointerIcon {
	companion object {
		val Default: PointerIcon = object : PointerIcon {}
		val Crosshair: PointerIcon = object : PointerIcon {}
		val Text: PointerIcon = object : PointerIcon {}
		val Hand: PointerIcon = object : PointerIcon {}
	}
}

// PointerKeyboardModifiers is vendored (see PointerEvent.kt).

/** Upstream `PointerIconService` — set the platform pointer cursor. */
interface PointerIconService {
	fun getIcon(): PointerIcon = PointerIcon.Default
	fun setIcon(value: PointerIcon?) {}
	fun getStylusHoverIcon(): PointerIcon? = null
	fun setStylusHoverIcon(value: PointerIcon?) {}
}
