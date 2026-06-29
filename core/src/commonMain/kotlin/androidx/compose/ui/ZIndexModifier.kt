package androidx.compose.ui

import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.unit.Constraints

// ==================
// MARK: zIndex
// ==================

/**
 * Creates a modifier that controls the drawing order for the children of the
 * same layout parent. A child with larger [zIndex] will be drawn on top of all
 * the children with smaller [zIndex]. When children have the same [zIndex] the
 * original order in which the parent placed the children is used.
 *
 * Note that if there would be multiple [zIndex] modifiers applied for the same
 * layout the sum of their values will be used as the final zIndex. If no
 * [zIndex] were applied for the layout then the default zIndex is 0.
 */
@Stable
fun Modifier.zIndex(zIndex: Float): Modifier =
	if (zIndex == 0f) this else this.then(ZIndexElement(zIndex = zIndex))

/**
 * `ModifierNodeElement` factory for the project's z-ordering modifier. Mirrors
 * upstream's `ZIndexElement` (also `internal`) at
 * `compose/ui/ui/.../androidx/compose/ui/ZIndexModifier.kt:42`.
 *
 * The project's renderer reads the value by walking `Modifier.foldIn` and
 * summing every `ZIndexElement.zIndex` — see
 * `com.compose.desktop.native.node.LayoutNode.zIndex`. The paired
 * [ZIndexNode] lifecycle is dormant until the renderer rewrite drives it via
 * the upstream `NodeCoordinator` layout pipeline.
 */
internal data class ZIndexElement(val zIndex: Float) : ModifierNodeElement<ZIndexNode>() {
	override fun create() = ZIndexNode(zIndex)
	override fun update(node: ZIndexNode) { node.zIndex = zIndex }
}

/**
 * Paired `Modifier.Node` for [ZIndexElement]. Marks itself as a
 * [LayoutModifierNode] to match upstream's shape — upstream's `measure()`
 * body forwards to the child via `placeable.place(0, 0, zIndex = zIndex)`.
 * Our renderer reads zIndex directly from the cached element instead, so
 * `measure()` here is a plain pass-through (the chain isn't yet driven by
 * the per-modifier coordinator).
 */
internal class ZIndexNode(var zIndex: Float) : Modifier.Node(), androidx.compose.ui.node.LayoutModifierNode {
	override fun MeasureScope.measure(
		measurable: Measurable,
		constraints: Constraints,
	): MeasureResult {
		val placeable = measurable.measure(constraints)
		return layout(placeable.width, placeable.height) { placeable.placeAt(0, 0) }
	}

	override fun toString(): String = "ZIndexNode(zIndex=$zIndex)"
}
