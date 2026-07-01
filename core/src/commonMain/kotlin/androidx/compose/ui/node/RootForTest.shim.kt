package androidx.compose.ui.node

// ==================
// MARK: RootForTest shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.node.RootForTest` — the test
 * infrastructure entry point. Real interface pulls SemanticsOwner /
 * TextInputService / KeyEvent / IndirectPointerEvent. We ship a bare
 * interface with the density accessor (only common denominator).
 */
interface RootForTest {
	val density: androidx.compose.ui.unit.Density
}
