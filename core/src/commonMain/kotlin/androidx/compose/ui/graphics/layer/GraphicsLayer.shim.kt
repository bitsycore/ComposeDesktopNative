package androidx.compose.ui.graphics.layer

// ==================
// MARK: GraphicsLayer shim
// ==================

/**
 * Stub for upstream `androidx.compose.ui.graphics.layer.GraphicsLayer` —
 * the expect class the vendored graphics-layer engine (`SkiaGraphicsLayer
 * .skiko.kt` / `AndroidGraphicsLayer.android.kt`) implements. Real class
 * is 437L common + 513L Skia actual — pulls Skiko + PlatformShadowContext
 * / ShadowContext.
 *
 * Our desktop path doesn't have a graphics-layer engine yet. Vendored
 * `Owner.createLayer(...)`, `OwnedLayer.drawLayer(Canvas, GraphicsLayer?)`,
 * and `Placeable.PlacementScope.placeWithLayer(..., layer: GraphicsLayer)`
 * all reference this type — marker satisfies them.
 */
class GraphicsLayer
