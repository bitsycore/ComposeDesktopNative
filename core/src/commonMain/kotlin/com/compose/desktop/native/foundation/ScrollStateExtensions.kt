package com.compose.desktop.native.foundation

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState

// ==================
// MARK: Project-only Scroll helpers
// ==================

/* Non-upstream extensions on ScrollState / LazyListState. Kept out of the
   androidx.compose.foundation namespace so that package stays a pure
   mirror of upstream's public API.

   Upstream's only "animated scroll" entry is the suspend
   `animateScrollBy(state, delta, animationSpec)` (in
   androidx.compose.foundation.gestures.ScrollExtensions.kt) — which
   requires a coroutine scope at the call site. Our mouse-wheel handler in
   :window runs in the main loop, not in a coroutine, so it needs a
   non-suspend animated-scroll entry. `smoothScrollByPx` is that entry —
   it accumulates the requested delta into ScrollState's
   `ScrollAnimator`-driven easing target and returns immediately. */

fun ScrollState.smoothScrollByPx(delta: Int) {
	smoothScrollByPxInternal(delta)
}

// ============
//  LazyListState pixel-based accessors
//
// Upstream's LazyListState is item-based (firstVisibleItemIndex /
// firstVisibleItemScrollOffset + suspend scrollToItem). Our impl is a
// thin pixel-based wrapper around a ScrollState, so these extensions
// expose the underlying offsets without inflating the upstream-named
// class's public surface.

val LazyListState.scrollOffsetPx: Int get() = scrollState.value
val LazyListState.maxScrollOffsetPx: Int get() = scrollState.maxValue
