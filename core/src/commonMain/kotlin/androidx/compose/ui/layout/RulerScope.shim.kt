package androidx.compose.ui.layout

// ==================
// MARK: RulerScope shim
// ==================

/**
 * Phase 4b shim for upstream `androidx.compose.ui.layout.RulerScope`
 * (defined inside upstream MeasureScope.kt — 171 lines, has the
 * `provides` / `providesRelative` builders for ruler values and
 * extends Density). Vendored MeasureResult references RulerScope only
 * in defaulted-null member types (`rulers: (RulerScope.() -> Unit)?`
 * etc.). Our renderers never construct a RulerScope, so this empty
 * interface is enough.
 *
 * Delete when our MeasureScope.kt is reshaped to upstream's full
 * signature (with `layout(width, height, alignmentLines, rulers,
 * placementBlock)`) and we vendor upstream MeasureScope.kt verbatim.
 */
interface RulerScope
