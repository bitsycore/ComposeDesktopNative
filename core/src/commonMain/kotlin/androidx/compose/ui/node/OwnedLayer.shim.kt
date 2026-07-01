package androidx.compose.ui.node

// ==================
// MARK: OwnedLayer shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.node.OwnedLayer` — a layer
 * returned by `Owner.createLayer(...)` to isolate drawn content. Real
 * interface has update / move / resize / drawLayer / etc. tied to the
 * graphics-layer engine. Marker satisfies `Owner.createLayer` return type.
 */
internal interface OwnedLayer
