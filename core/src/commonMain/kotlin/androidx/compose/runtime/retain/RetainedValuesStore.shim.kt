package androidx.compose.runtime.retain

// ==================
// MARK: RetainedValuesStore shim (runtime.retain)
// ==================

/**
 * Marker for upstream `androidx.compose.runtime.retain.RetainedValuesStore`.
 * A lifecycle-aware store for values that survive config changes /
 * activity recreations. Desktop has no lifecycle; a bare marker
 * satisfies `Owner.retainedValuesStore`.
 */
interface RetainedValuesStore
