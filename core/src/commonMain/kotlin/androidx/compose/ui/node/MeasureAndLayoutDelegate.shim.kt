package androidx.compose.ui.node

// ==================
// MARK: MeasureAndLayoutDelegate — minimal stub (non-official)
// ==================

/**
 * Upstream's `MeasureAndLayoutDelegate.kt` (892 lines) is the layout-pass
 * coordinator — owns the relayout / remeasure queues, drives the
 * `MeasurePassDelegate` / `LookaheadPassDelegate` per node, dispatches
 * the `OnPositionedDispatcher`, and exposes
 * `MeasureAndLayoutDelegate.PostponedRequest` as a nested class.
 *
 * The project doesn't have a layout state machine — `LayoutNode.measure`
 * and `LayoutNode.place` run synchronously from the renderer's per-frame
 * loop. So none of this delegate is wired up here; we expose only the
 * surface that vendored sibling files (`LayoutTreeConsistencyChecker`
 * via `MeasureAndLayoutDelegate.PostponedRequest`) read.
 *
 * Retires when the real delegate lands (Phase 9 LayoutNode swap).
 */
internal class MeasureAndLayoutDelegate {
	/** The triple of (node, isLookahead, isForced) that upstream queues
	 *  when a remeasure / relayout request can't be served right away. */
	class PostponedRequest(
		val node: LayoutNode,
		val isLookahead: Boolean,
		val isForced: Boolean,
	)
}
