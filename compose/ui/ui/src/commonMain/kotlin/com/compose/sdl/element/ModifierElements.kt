package com.compose.sdl.element

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.node.ModifierNodeElement

// ==================
// MARK: Project modifier elements
// ==================
// Small set of project-only modifier elements — each pairs a
// `ModifierNodeElement<XxxNode>` with a `XxxNode : Modifier.Node`. Upstream
// LayoutNode reads them through the chain (ModifierNodeElement IS-A
// Modifier.Element). Everything gesture-related has been removed — pointer
// input, text input, right/middle click, drag: all go through the standard
// `Modifier.pointerInput` + `detectTapGestures` / `detectDragGestures` /
// `awaitPointerEventScope` APIs upstream now supplies. What's left is the
// two elements the renderer still owns directly:
//   * ClipModifier — GraphicsLayer.kt lowers `clip = true` to it.
//   * GraphicsLayerModifier — the transform / alpha / cache pipeline.

class ClipModifier(val shape: Shape) : ModifierNodeElement<ClipNode>() {
    override fun create() = ClipNode(shape)
    override fun update(node: ClipNode) { node.shape = shape }
    override fun hashCode(): Int = shape.hashCode()
    override fun equals(other: Any?): Boolean = other is ClipModifier && other.shape == shape
}
class ClipNode(var shape: Shape) : Modifier.Node()

// ==================
// MARK: GraphicsLayerModifier
// ==================

/**
 * A "graphics layer" element: alpha + 2D transform (scale / rotation /
 * translation), with an optional cacheKey that opts the subtree into
 * render-to-texture caching across frames. See `Modifier.graphicsLayer`
 * (in `androidx.compose.ui.graphics`) for the caching semantics.
 *
 * The renderer reads this element directly via the `LayoutNode.graphicsLayer`
 * `foldIn` over the chain; the paired [GraphicsLayerNode] lifecycle stays
 * dormant until the renderer rewrite drives it.
 */
class GraphicsLayerModifier(
    val alpha: Float = 1f,
    val scaleX: Float = 1f,
    val scaleY: Float = 1f,
    val rotationZ: Float = 0f,
    val translationX: Float = 0f,
    val translationY: Float = 0f,
    val transformOrigin: TransformOrigin = TransformOrigin.Center,
    val cacheKey: Any? = null,
) : ModifierNodeElement<GraphicsLayerNode>() {

    val needsLayer: Boolean
        get() = alpha < 1f || cacheKey != null

    val needsTransform: Boolean
        get() = scaleX != 1f || scaleY != 1f || rotationZ != 0f ||
            translationX != 0f || translationY != 0f

    val isIdentity: Boolean
        get() = !needsLayer && !needsTransform

    override fun create() =
        GraphicsLayerNode(alpha, scaleX, scaleY, rotationZ, translationX, translationY, transformOrigin, cacheKey)
    override fun update(node: GraphicsLayerNode) {
        node.alpha = alpha; node.scaleX = scaleX; node.scaleY = scaleY
        node.rotationZ = rotationZ; node.translationX = translationX; node.translationY = translationY
        node.transformOrigin = transformOrigin; node.cacheKey = cacheKey
    }
    override fun hashCode(): Int {
        var v = alpha.hashCode()
        v = 31 * v + scaleX.hashCode(); v = 31 * v + scaleY.hashCode()
        v = 31 * v + rotationZ.hashCode()
        v = 31 * v + translationX.hashCode(); v = 31 * v + translationY.hashCode()
        v = 31 * v + transformOrigin.hashCode(); v = 31 * v + (cacheKey?.hashCode() ?: 0)
        return v
    }
    override fun equals(other: Any?): Boolean =
        other is GraphicsLayerModifier &&
            other.alpha == alpha && other.scaleX == scaleX && other.scaleY == scaleY &&
            other.rotationZ == rotationZ &&
            other.translationX == translationX && other.translationY == translationY &&
            other.transformOrigin == transformOrigin && other.cacheKey == cacheKey
}

class GraphicsLayerNode(
    var alpha: Float,
    var scaleX: Float,
    var scaleY: Float,
    var rotationZ: Float,
    var translationX: Float,
    var translationY: Float,
    var transformOrigin: TransformOrigin,
    var cacheKey: Any?,
) : Modifier.Node()
