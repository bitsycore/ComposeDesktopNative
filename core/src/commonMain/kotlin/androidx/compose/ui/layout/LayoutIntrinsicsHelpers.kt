package androidx.compose.ui.layout

import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection

// ==================
// MARK: Intrinsic helpers (project-shape, upstream parity)
// ==================
//
// Upstream defines these as `internal` types inside its 419-line
// `Layout.kt`. We don't vendor that file (it needs upstream LayoutNode),
// so we recreate the helpers here. Bodies mirror upstream's
// `IntrinsicMinMax` / `IntrinsicWidthHeight` enums + `LargeDimension`
// constant + `DefaultIntrinsicMeasurable` + `IntrinsicsMeasureScope` +
// `FixedSizeIntrinsicsPlaceable` byte-for-byte where the upstream code
// doesn't pull engine bits.

/** Mirror of upstream Layout.kt:277 — `IntrinsicMeasurable` min-or-max marker. */
internal enum class IntrinsicMinMax { Min, Max }

/** Mirror of upstream Layout.kt:283 — `IntrinsicMeasurable` width-or-height marker. */
internal enum class IntrinsicWidthHeight { Width, Height }

/**
 * Mirror of upstream Layout.kt:294 — large value used as a finite replacement
 * for `Constraints.Infinity` inside intrinsic measurements.
 */
internal const val LargeDimension: Int = (1 shl 15) - 1

/**
 * Mirror of upstream Layout.kt:301 — Measurable wrapper used by intrinsic
 * measurements: it surfaces a child's intrinsic width/height as the
 * "measured" size, so a parent's `MeasurePolicy.measure` body can be
 * re-run during the intrinsics pass to compute layouts under hypothetical
 * widths/heights.
 */
internal class DefaultIntrinsicMeasurable(
	val measurable: IntrinsicMeasurable,
	private val minMax: IntrinsicMinMax,
	private val widthHeight: IntrinsicWidthHeight,
) : Measurable {
	override val parentData: Any?
		get() = measurable.parentData

	override fun measure(constraints: Constraints): Placeable {
		if (widthHeight == IntrinsicWidthHeight.Width) {
			val width =
				if (minMax == IntrinsicMinMax.Max) measurable.maxIntrinsicWidth(constraints.maxHeight)
				else measurable.minIntrinsicWidth(constraints.maxHeight)
			val height = if (constraints.hasBoundedHeight) constraints.maxHeight else LargeDimension
			return FixedSizeIntrinsicsPlaceable(width, height)
		}
		val height =
			if (minMax == IntrinsicMinMax.Max) measurable.maxIntrinsicHeight(constraints.maxWidth)
			else measurable.minIntrinsicHeight(constraints.maxWidth)
		val width = if (constraints.hasBoundedWidth) constraints.maxWidth else LargeDimension
		return FixedSizeIntrinsicsPlaceable(width, height)
	}

	override fun minIntrinsicWidth(height: Int): Int = measurable.minIntrinsicWidth(height)
	override fun maxIntrinsicWidth(height: Int): Int = measurable.maxIntrinsicWidth(height)
	override fun minIntrinsicHeight(width: Int): Int = measurable.minIntrinsicHeight(width)
	override fun maxIntrinsicHeight(width: Int): Int = measurable.maxIntrinsicHeight(width)
}

/**
 * Mirror of upstream Layout.kt:255 — Placeable that reports a fixed size
 * without doing real placement work. Used by intrinsics; placing it is a
 * no-op (the value isn't part of the real layout pass).
 */
internal class FixedSizeIntrinsicsPlaceable(
	override val width: Int,
	override val height: Int,
) : Placeable() {
	override fun placeAt(inX: Int, inY: Int) { /* no-op — intrinsics don't place */ }
}

/**
 * Mirror of upstream Layout.kt:353 — wraps an `IntrinsicMeasureScope` so a
 * `MeasureScope.measure(...)` block can be re-run during the intrinsics
 * pass. Returns a [MeasureResult] whose `placeChildren()` body never runs
 * (the result is only used for its `width`/`height`).
 */
internal class IntrinsicsMeasureScope(
	intrinsicMeasureScope: IntrinsicMeasureScope,
	override val layoutDirection: LayoutDirection,
) : MeasureScope, IntrinsicMeasureScope by intrinsicMeasureScope {
	override fun layout(
		width: Int,
		height: Int,
		alignmentLines: Map<AlignmentLine, Int>,
		placementBlock: Placeable.PlacementScope.() -> Unit,
	): MeasureResult {
		val w = width.coerceAtLeast(0)
		val h = height.coerceAtLeast(0)
		return object : MeasureResult {
			override val width = w
			override val height = h
			override val alignmentLines = alignmentLines
			override fun placeChildren() { /* no-op during intrinsics */ }
		}
	}
}
