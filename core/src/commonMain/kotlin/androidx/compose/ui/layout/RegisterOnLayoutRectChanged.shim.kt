package androidx.compose.ui.layout

import androidx.compose.ui.node.DelegatableNode

// ==================
// MARK: registerOnLayoutRectChanged shim
// ==================

/**
 * Phase 2 shim for upstream `Modifier.Node.registerOnLayoutRectChanged`
 * extension function.
 *
 * UnplacedAwareModifierNode imports this symbol only for a KDoc
 * cross-reference. Real upstream impl lives in RegisterOnLayoutRectChanged.kt
 * and pulls in the rect-tracker engine. No-op stub here so the import
 * resolves.
 *
 * Delete when the real registerOnLayoutRectChanged is vendored.
 */
@Suppress("UNUSED_PARAMETER")
internal fun DelegatableNode.registerOnLayoutRectChanged(
	inThrottleMillis: Long = 0,
	inDebounceMillis: Long = 0,
	inCallback: (LayoutCoordinates) -> Unit,
) { /* phase 2 no-op */ }
