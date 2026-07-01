package androidx.compose.ui.graphics.drawscope

import androidx.compose.ui.graphics.Canvas

// ==================
// MARK: drawContext + drawIntoCanvas (shims for vendored consumers)
// ==================
//
// Upstream `DrawScope` exposes `drawContext: DrawContext { val canvas: Canvas; ... }`
// and an inline extension `DrawScope.drawIntoCanvas(block: (Canvas) -> Unit)`.
// Our project DrawScope doesn't yet host these; vendored `GraphicsLayer.kt`
// / other files reach for them. These shims satisfy the type/import path
// without wiring a real canvas.

/**
 * Marker for upstream `androidx.compose.ui.graphics.drawscope.DrawContext`.
 * Real class carries canvas / size / density / layoutDirection / transform.
 * Vendored `GraphicsLayer` only reads `drawContext.canvas` and only inside
 * paths our project's DrawScope never enters (no GraphicsLayer construction
 * yet). A stub property + throw-on-access canvas is sufficient.
 */
class DrawContext {
	val canvas: Canvas get() = throw NotImplementedError("DrawContext.canvas — no GraphicsLayer engine yet")
	/** Upstream `DrawContext.graphicsLayer: GraphicsLayer?` — the layer
	 *  currently being drawn into. Always null in our path (no
	 *  GraphicsLayer construction yet). */
	val graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer? get() = null
}

/** Upstream `inline fun DrawScope.drawIntoCanvas(block: (Canvas) -> Unit)`. */
inline fun DrawScope.drawIntoCanvas(@Suppress("UNUSED_PARAMETER") block: (Canvas) -> Unit) {
	// No-op: our DrawScope doesn't expose a Canvas. Vendored consumers
	// that would call this are dormant (no GraphicsLayer construction).
}
