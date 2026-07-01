package androidx.compose.ui.node

// ==================
// MARK: LayoutNodeDrawScope shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.node.LayoutNodeDrawScope`.
 * Real class (152L) is a `ContentDrawScope` implementation that walks
 * the modifier chain drawing each `DrawModifierNode`. Our project's
 * renderers do this walk directly (Phase 8 wiring in the SkiaRenderer /
 * Sdl3Renderer's per-node draw loop) — this marker just satisfies
 * `Owner.sharedDrawScope`.
 */
internal class LayoutNodeDrawScope
