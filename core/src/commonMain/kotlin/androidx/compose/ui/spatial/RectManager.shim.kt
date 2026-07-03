package androidx.compose.ui.spatial

import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.unit.IntOffset

const val NotFound: Int = -1

// Phase 9 stub — upstream RectManager is a spatial index of node bounding rects. No-op.
class RectManager {
	internal fun recalculateRectIfDirty(node: LayoutNode) {}
	internal fun getOffsetFromRectListFor(node: LayoutNode): IntOffset = IntOffset.Max
	internal fun updateFlagsFor(node: LayoutNode, hasFocusTarget: Boolean, hasPointerInput: Boolean) {}
	internal fun invalidateCallbacksFor(node: LayoutNode) {}
	internal fun remove(node: LayoutNode) {}

	// Focus-by-rect search (upstream RectManager spatially indexes focus targets). No spatial
	// index here -> no candidate; focus traversal falls back to the tree walk.
	internal fun findFocusableNodeFromRect(
		left: Int, top: Int, right: Int, bottom: Int, containerId: Int,
	): androidx.compose.ui.focus.FocusTargetNode? = null
}
