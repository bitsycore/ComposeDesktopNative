package com.compose.desktop.native.scroll

import androidx.compose.foundation.ScrollState

// ==================
// MARK: ScrollAnimator
// ==================

/* Per-frame driver for ScrollState.smoothScrollBy easing. This is project
   infrastructure with no official Compose equivalent, so it lives in the
   com.compose.desktop.native layer rather than androidx.compose.foundation.

   ScrollStates register here when a smooth scroll starts; the window's main
   loop calls tick() once per frame and states drop out when they reach their
   target. No-op (cheap) when idle. */
object ScrollAnimator {
    private val fActive = mutableSetOf<ScrollState>()

    fun register(inState: ScrollState) { fActive.add(inState) }

    fun tick() {
        if (fActive.isEmpty()) return
        val vIt = fActive.iterator()
        while (vIt.hasNext()) {
            if (!vIt.next().tickSmooth()) vIt.remove()
        }
    }
}
