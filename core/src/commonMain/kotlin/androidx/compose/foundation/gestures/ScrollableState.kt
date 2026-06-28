package androidx.compose.foundation.gestures

import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.MutatorMutex

// ==================
// MARK: ScrollScope
// ==================

/* Scope provided to ScrollableState.scroll {} blocks. Inside you call
   `scrollBy(pixels)` to advance the scroll position; the call returns the
   number of pixels actually consumed (after clamping at edges, etc.).
   Matches the upstream `androidx.compose.foundation.gestures.ScrollScope`
   signature; full nested-scroll / fling pipeline is upstream-only. */
interface ScrollScope {
	fun scrollBy(pixels: Float): Float
}

// ==================
// MARK: ScrollableState
// ==================

/* Anything you can scroll. ScrollState and (in upstream) LazyListState /
   LazyGridState all implement this. The contract:

   - All position-changing operations must go through `scroll {}`, which
     uses a MutatorMutex to enforce mutual exclusion at the given
     MutatePriority — a higher-priority scroll cancels the in-flight one.
   - `dispatchRawDelta(delta)` is the escape hatch: it bypasses the mutex
     for low-level integrations (nested scroll, snap-back). Use sparingly. */
interface ScrollableState {

	/* Take exclusive control of scrolling at the given priority and run the
	   block. Concurrent calls with priority >= ongoing one cancel the
	   ongoing scroll. */
	suspend fun scroll(
		scrollPriority: MutatePriority = MutatePriority.Default,
		block: suspend ScrollScope.() -> Unit,
	)

	/* Push a raw delta in pixels; returns the delta actually consumed.
	   Bypasses the mutex — for low-level use only. */
	fun dispatchRawDelta(delta: Float): Float

	/* Is a scroll currently in progress (drag / fling / programmatic)? */
	val isScrollInProgress: Boolean

	/* Can this scrollable currently move forward (down/right) along its axis? */
	val canScrollForward: Boolean
		get() = true

	/* Can this scrollable currently move backward (up/left) along its axis? */
	val canScrollBackward: Boolean
		get() = true
}
