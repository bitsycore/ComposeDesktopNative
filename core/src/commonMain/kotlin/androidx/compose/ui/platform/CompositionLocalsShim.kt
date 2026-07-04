@file:Suppress("UNUSED")

package androidx.compose.ui.platform

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// ==================
// MARK: CompositionLocals — minimal project shim (non-official)
// ==================

/*
 * Stub of selected entries from upstream's
 * `androidx.compose.ui.platform.CompositionLocals.kt`. The full file
 * pulls Owner / InputModeManager / ViewConfiguration / Autofill /
 * FontFamilyResolver / SoftwareKeyboardController / TextToolbar and
 * other heavy types we don't have yet. Vendored downstream code
 * (Indication, Background, BasicMarquee, SuspendingPointerInputFilter)
 * only reads these locals occasionally, so we expose just the ones
 * with sensible cross-platform defaults.
 *
 * Replace with the full vendored file when its engine deps land.
 */

/**
 * `LocalDensity.current` — the project doesn't bind density per
 * composition (the renderer applies HiDPI scaling separately), so a
 * fixed `Density(1f, 1f)` default is the right reading for layout-time
 * `Dp.toPx()` etc.
 */
val LocalDensity = staticCompositionLocalOf<Density> { Density(1f, 1f) }

/** Layout direction — Ltr until RTL lands in the renderer. */
val LocalLayoutDirection = staticCompositionLocalOf<LayoutDirection> { LayoutDirection.Ltr }

/** Haptic feedback local — no-op default (SDL desktop has no haptics); Clickable long-press reads it. */
val LocalHapticFeedback = staticCompositionLocalOf<androidx.compose.ui.hapticfeedback.HapticFeedback> {
	object : androidx.compose.ui.hapticfeedback.HapticFeedback {
		override fun performHapticFeedback(hapticFeedbackType: androidx.compose.ui.hapticfeedback.HapticFeedbackType) {}
	}
}

/** Platform click/interaction sound (SoundEffect is vendored) — no-op default on SDL desktop. */
val LocalSoundEffect = staticCompositionLocalOf<SoundEffect> {
	object : SoundEffect { override fun playClickSound() {} }
}

/** Whether a scroll-capture (accessibility screenshot scrolling) is in progress. Upstream declares
 *  it in the unvendored CompositionLocals.kt; no scroll-capture on SDL, so always false. */
val LocalScrollCaptureInProgress = staticCompositionLocalOf<Boolean> { false }

/** Whether the text-field cursor should blink. Default true; renderer reads via
 *  TextFieldCoreModifier for the cursor draw pass. */
val LocalCursorBlinkEnabled = staticCompositionLocalOf<Boolean> { true }

/** Text-selection toolbar local — no-op default on desktop (SelectionManager reads it to show
 *  the floating text menu on mobile long-press; on desktop context menu is opened via right-click
 *  through the vendored foundation.text.contextmenu tree instead).
 *  TODO: wire a real TextToolbar impl if we grow a desktop selection UX that wants it. */
/** FontFamily resolver — resolves declarative FontFamily specs to concrete platform
 *  typefaces. Our text renderer reads `style.fontFamily` (name-based, project's
 *  `FontFamily.Named`) directly rather than routing through the Resolver, so the
 *  default just hands back a placeholder State<Any>. Vendored BasicText /
 *  TextStringSimpleElement / TextAnnotatedStringElement read it via
 *  `LocalFontFamilyResolver.current` but don't actually consume the resolved value
 *  for our text pipeline.
 *  TODO: wire a real resolver once we grow a proper typeface-loading pipeline. */
val LocalFontFamilyResolver = staticCompositionLocalOf<androidx.compose.ui.text.font.FontFamily.Resolver> {
	androidx.compose.ui.text.font.projectFontFamilyResolver
}

/** URI open handler — the vendored TextLinkScope reads it via `LocalUriHandler.current`
 *  to handle clicks on `LinkAnnotation` spans in AnnotatedString. Default routes via
 *  the project's OpenExternal helper (SDL_OpenURL under the hood).
 *  TODO: replace with vendored `ui.platform.CompositionLocals.kt` once the whole
 *  CompositionLocals surface can vendor. */
val LocalUriHandler = staticCompositionLocalOf<UriHandler> { defaultUriHandler }

// The active default UriHandler — see `installDefaultUriHandler` (nativeMain sets
// this to a SDL_OpenURL-backed impl at renderer init). Kept in a mutable global
// so we can override from nativeMain without an expect/actual dance.
internal var defaultUriHandler: UriHandler = object : UriHandler {
	override fun openUri(uri: String) {
		// TODO: renderer init should install the SDL3 impl via `installDefaultUriHandler`
	}
}

/** Called by nativeMain at renderer init to wire the SDL3 URL handler. */
fun installDefaultUriHandler(handler: UriHandler) {
	defaultUriHandler = handler
}

/** Alias of `androidx.compose.ui.focus.LocalFocusManager` under `ui.platform`
 *  — upstream declares it in `ui.platform.CompositionLocals.kt` but our
 *  project puts it in `ui.focus.FocusManagerLocal.kt`. Vendored files
 *  reference the ui.platform path. */
val LocalFocusManager
	get() = androidx.compose.ui.focus.LocalFocusManager

/** Software keyboard controller — no keyboard on desktop; returns null default. */
val LocalSoftwareKeyboardController = staticCompositionLocalOf<SoftwareKeyboardController?> { null }

/** Window info surfacing focus / IME state. Project has no window-info source yet;
 *  a no-op impl unblocks vendored TextField code that reads
 *  `LocalWindowInfo.current.isWindowFocused`. TODO: wire real window focus. */
val LocalWindowInfo = staticCompositionLocalOf<WindowInfo> {
	object : WindowInfo {
		override val isWindowFocused: Boolean = true
	}
}

val LocalTextToolbar = staticCompositionLocalOf<TextToolbar> {
	object : TextToolbar {
		override fun showMenu(
			rect: androidx.compose.ui.geometry.Rect,
			onCopyRequested: (() -> Unit)?,
			onPasteRequested: (() -> Unit)?,
			onCutRequested: (() -> Unit)?,
			onSelectAllRequested: (() -> Unit)?,
		) { /* no toolbar on desktop */ }
		override fun hide() {}
		override val status: TextToolbarStatus get() = TextToolbarStatus.Hidden
	}
}
