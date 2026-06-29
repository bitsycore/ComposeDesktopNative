package androidx.compose.ui.node

import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

// ==================
// MARK: LayoutModifierNode (project-shape, upstream parity)
// ==================

/**
 * Project version of upstream `androidx.compose.ui.node.LayoutModifierNode`.
 * The public signature matches upstream LayoutModifierNode.kt:50-103 for
 * the non-Approach surface (`measure` + four `IntrinsicMeasureScope`
 * defaults). The Approach* variants upstream ships
 * (`approachMeasure`/`Approach*IntrinsicWidth/Height` defaults) are
 * omitted — they require `LayoutModifierNodeCoordinator` +
 * `LookaheadLayoutCoordinates`, which our single-coordinator NodeChain
 * doesn't have.
 *
 * Intrinsic defaults route through [NodeMeasuringIntrinsics], which is a
 * project-side recreation of upstream's internal object (the one that
 * sits inside upstream's `LayoutModifierNode.kt`, alongside the
 * interface). Behaviour for non-Approach nodes is byte-identical to
 * upstream.
 *
 * Retires `LayoutModifierNode.shim.kt`.
 */
interface LayoutModifierNode : DelegatableNode {

	fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult

	fun IntrinsicMeasureScope.minIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
		NodeMeasuringIntrinsics.minWidth({ m, c -> measure(m, c) }, this, measurable, height)

	fun IntrinsicMeasureScope.minIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
		NodeMeasuringIntrinsics.minHeight({ m, c -> measure(m, c) }, this, measurable, width)

	fun IntrinsicMeasureScope.maxIntrinsicWidth(measurable: IntrinsicMeasurable, height: Int): Int =
		NodeMeasuringIntrinsics.maxWidth({ m, c -> measure(m, c) }, this, measurable, height)

	fun IntrinsicMeasureScope.maxIntrinsicHeight(measurable: IntrinsicMeasurable, width: Int): Int =
		NodeMeasuringIntrinsics.maxHeight({ m, c -> measure(m, c) }, this, measurable, width)
}

/**
 * Upstream `LayoutModifierNode.kt:117` — invalidates the node's layer.
 * Our coordinator doesn't own a layer; no-op until the renderer rewrite
 * uses the per-modifier coordinator chain.
 */
fun LayoutModifierNode.invalidateLayer() { /* no-op */ }

/** Upstream LayoutModifierNode.kt:123 — invalidates placement; no-op for the same reason. */
fun LayoutModifierNode.invalidatePlacement() { /* no-op */ }

/** Upstream LayoutModifierNode.kt:129 — invalidates measurement; no-op for the same reason. */
fun LayoutModifierNode.invalidateMeasurement() { /* no-op */ }
