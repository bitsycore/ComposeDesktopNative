package androidx.compose.ui.platform

// ==================
// MARK: Clipboard
// ==================

/* Reduced re-implementation of the official androidx.compose.ui.platform
   Clipboard. Official exposes a suspend, ClipEntry-based surface
   (getClipEntry / setClipEntry / nativeClipboard); this project ships a
   simplified synchronous text-only shape until the full reshape lands. Name +
   package are kept official so call sites and LocalClipboard track upstream.

   The native backend installs an SDL3-backed impl during composeWindow
   startup; commonMain holds a no-op default so tests / unset configurations
   don't crash on read. */
interface Clipboard {
    fun getText(): String?
    fun setText(text: String)
}

private object NoOpClipboard : Clipboard {
    override fun getText(): String? = null
    override fun setText(text: String) {}
}

/* Mutable wiring global the native backend points at its SDL3 clipboard at
   startup; read from non-composition code (event handlers, coroutines). Not
   part of official Compose — it's an irreducible project glue global, the same
   pattern as currentImageLoader / currentTextMeasurer. Inside composition
   prefer LocalClipboardManager.current. */
var currentClipboard: Clipboard = NoOpClipboard
