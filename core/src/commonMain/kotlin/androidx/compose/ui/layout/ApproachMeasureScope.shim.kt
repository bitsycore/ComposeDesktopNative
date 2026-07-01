package androidx.compose.ui.layout

// ==================
// MARK: Approach* shims (non-official)
// ==================

/**
 * Upstream `ApproachIntrinsicMeasureScope` / `ApproachMeasureScope` live
 * in the same-named upstream file plus `ApproachLayoutModifierNode.kt`
 * (unvendored — they carry the lookahead-approach pipeline). Vendored
 * `Layout.kt` declares `ApproachIntrinsicsMeasureScope` at the tail — a
 * scope that combines both interfaces. Nothing in our project constructs
 * one; it's only referenced as a type.
 *
 * These shims mark the marker interfaces so type resolution succeeds.
 * Retires when the full lookahead-approach pass lands (Phase 11+).
 */
interface ApproachIntrinsicMeasureScope : IntrinsicMeasureScope {
	val lookaheadSize: androidx.compose.ui.unit.IntSize
}

interface ApproachMeasureScope : ApproachIntrinsicMeasureScope, MeasureScope
