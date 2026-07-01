package androidx.compose.ui.spatial

// ==================
// MARK: RectManager shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.spatial.RectManager` — a queryable
 * spatial index of node bounding rects. Real class exposes rects/
 * registerOnGlobalLayoutCallback/etc.; we ship a bare class so
 * `Owner.rectManager` resolves.
 */
class RectManager
