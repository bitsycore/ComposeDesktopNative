package androidx.compose.ui.semantics

// ==================
// MARK: SemanticsOwner shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.semantics.SemanticsOwner`.
 * Real class exposes root/`unmergedRootSemanticsNode`/`getAllSemanticsNodes`
 * / `getLayoutRectForNode` / semantics listener registration. The desktop
 * a11y layer isn't wired, so a marker is enough for vendored `Owner.kt`
 * to declare `val semanticsOwner: SemanticsOwner`.
 */
class SemanticsOwner
