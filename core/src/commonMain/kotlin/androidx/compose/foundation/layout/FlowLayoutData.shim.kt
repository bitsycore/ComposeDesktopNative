package androidx.compose.foundation.layout

// ==================
// MARK: FlowLayoutData — extracted shim (non-official)
// ==================

/**
 * Upstream `FlowLayoutData` lives inside `FlowLayout.kt` (1584 lines) —
 * an internal container for the fill-cross-axis fraction that Row/Column
 * children set via the `Modifier.fillMaxCrossAxis(fraction)` extension
 * on the FlowLayout scope.
 *
 * Vendored `RowColumnMeasurePolicy` reads `RowColumnParentData.flowLayoutData?
 * .fillCrossAxisFraction` to size children that fill the cross axis
 * relative to the parent's cross-axis max. We don't have FlowLayout, so
 * this stays null in practice — but the type has to resolve for
 * RowColumnImpl / RowColumnMeasurePolicy to compile.
 *
 * Retires when FlowLayout.kt is vendored (Phase 11+).
 */
internal data class FlowLayoutData(var fillCrossAxisFraction: Float)
