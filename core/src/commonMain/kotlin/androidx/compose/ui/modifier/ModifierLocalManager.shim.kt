package androidx.compose.ui.modifier

// ==================
// MARK: ModifierLocalManager shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.modifier.ModifierLocalManager`.
 * Real class coalesces ModifierLocal updates and dispatches them at
 * end-of-frame; we ship a marker with no-op methods so vendored Owner
 * has a value to expose.
 */
class ModifierLocalManager(@Suppress("UNUSED_PARAMETER") owner: Any) {
	fun invalidate() {}
	fun insertedProvider(
		@Suppress("UNUSED_PARAMETER") node: Any,
		@Suppress("UNUSED_PARAMETER") key: ModifierLocal<*>,
	) {}
	fun removedProvider(
		@Suppress("UNUSED_PARAMETER") node: Any,
		@Suppress("UNUSED_PARAMETER") key: ModifierLocal<*>,
	) {}
	fun updatedProvider(
		@Suppress("UNUSED_PARAMETER") node: Any,
		@Suppress("UNUSED_PARAMETER") key: ModifierLocal<*>,
	) {}
}
