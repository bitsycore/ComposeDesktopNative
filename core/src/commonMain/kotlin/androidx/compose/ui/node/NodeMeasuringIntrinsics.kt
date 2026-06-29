package androidx.compose.ui.node

import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.IntrinsicsMeasureScope
import androidx.compose.ui.layout.IntrinsicMinMax
import androidx.compose.ui.layout.IntrinsicWidthHeight
import androidx.compose.ui.layout.DefaultIntrinsicMeasurable
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.unit.Constraints

// ==================
// MARK: NodeMeasuringIntrinsics (project subset, upstream parity)
// ==================

/**
 * Mirror of upstream `androidx.compose.ui.node.NodeMeasuringIntrinsics`,
 * which lives as an `internal object` inside upstream's
 * `LayoutModifierNode.kt`. We need it accessible separately so
 * [LayoutModifierNode]'s intrinsic defaults can route through it without
 * pulling the Approach* per-modifier coordinator chain.
 *
 * Upstream ships **two** measure-block variants: a plain `MeasureBlock`
 * (`MeasureScope.measure(...)`) and an `ApproachMeasureBlock`
 * (`ApproachMeasureScope.measure(...)`). Our single-coordinator engine
 * doesn't drive an Approach path, so this version keeps only the plain
 * variant — the IntrinsicMeasureScope-based one used by every
 * non-Approach `LayoutModifierNode`. Bodies match upstream's
 * `MeasureBlock` overloads at LayoutModifierNode.kt:242-303.
 */
internal object NodeMeasuringIntrinsics {

	internal fun interface MeasureBlock {
		fun MeasureScope.measure(measurable: Measurable, constraints: Constraints): MeasureResult
	}

	internal fun minWidth(
		measureBlock: MeasureBlock,
		intrinsicMeasureScope: IntrinsicMeasureScope,
		intrinsicMeasurable: IntrinsicMeasurable,
		h: Int,
	): Int {
		val m = DefaultIntrinsicMeasurable(intrinsicMeasurable, IntrinsicMinMax.Min, IntrinsicWidthHeight.Width)
		val constraints = Constraints(maxHeight = h)
		val result = with(measureBlock) {
			IntrinsicsMeasureScope(intrinsicMeasureScope, intrinsicMeasureScope.layoutDirection).measure(m, constraints)
		}
		return result.width
	}

	internal fun minHeight(
		measureBlock: MeasureBlock,
		intrinsicMeasureScope: IntrinsicMeasureScope,
		intrinsicMeasurable: IntrinsicMeasurable,
		w: Int,
	): Int {
		val m = DefaultIntrinsicMeasurable(intrinsicMeasurable, IntrinsicMinMax.Min, IntrinsicWidthHeight.Height)
		val constraints = Constraints(maxWidth = w)
		val result = with(measureBlock) {
			IntrinsicsMeasureScope(intrinsicMeasureScope, intrinsicMeasureScope.layoutDirection).measure(m, constraints)
		}
		return result.height
	}

	internal fun maxWidth(
		measureBlock: MeasureBlock,
		intrinsicMeasureScope: IntrinsicMeasureScope,
		intrinsicMeasurable: IntrinsicMeasurable,
		h: Int,
	): Int {
		val m = DefaultIntrinsicMeasurable(intrinsicMeasurable, IntrinsicMinMax.Max, IntrinsicWidthHeight.Width)
		val constraints = Constraints(maxHeight = h)
		val result = with(measureBlock) {
			IntrinsicsMeasureScope(intrinsicMeasureScope, intrinsicMeasureScope.layoutDirection).measure(m, constraints)
		}
		return result.width
	}

	internal fun maxHeight(
		measureBlock: MeasureBlock,
		intrinsicMeasureScope: IntrinsicMeasureScope,
		intrinsicMeasurable: IntrinsicMeasurable,
		w: Int,
	): Int {
		val m = DefaultIntrinsicMeasurable(intrinsicMeasurable, IntrinsicMinMax.Max, IntrinsicWidthHeight.Height)
		val constraints = Constraints(maxWidth = w)
		val result = with(measureBlock) {
			IntrinsicsMeasureScope(intrinsicMeasureScope, intrinsicMeasureScope.layoutDirection).measure(m, constraints)
		}
		return result.height
	}
}
