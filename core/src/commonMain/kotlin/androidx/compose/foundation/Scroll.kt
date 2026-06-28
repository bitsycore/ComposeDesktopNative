package androidx.compose.foundation

import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.compose.desktop.native.element.HorizontalScrollModifier
import androidx.compose.ui.Modifier
import com.compose.desktop.native.element.VerticalScrollModifier
import com.compose.desktop.native.scroll.ScrollAnimator
import kotlin.math.roundToInt

// ==================
// MARK: ScrollState
// ==================

/* Holds a scroll offset and its currently-known max. Both are MutableState
   so any composable reading value / maxValue recomposes when they change.
   The owning Modifier.verticalScroll (or horizontalScroll) sets maxValue
   from the actual content height / width during layout.

   scrollBy / scrollTo move instantly (used by the scrollbar thumb drag);
   smoothScrollBy eases toward a target over several frames (used by the mouse
   wheel) — the window loop drives the easing once per frame via
   ScrollAnimator.tick(). */
class ScrollState(initial: Int = 0) : ScrollableState {
    private var _value by mutableStateOf(initial.coerceAtLeast(0))
    private var _maxValue by mutableStateOf(Int.MAX_VALUE)
    private var _viewportSize by mutableStateOf(0)
    // Smooth-scroll target the easing chases; kept == _value when not animating.
    private var _animTarget = initial.coerceAtLeast(0)
    private val scrollMutex = MutatorMutex()
    private var _isScrollInProgress by mutableStateOf(false)

    val value: Int get() = _value
    val maxValue: Int get() = _maxValue

    // ==================
    // MARK: ScrollableState
    // ==================

    override val isScrollInProgress: Boolean get() = _isScrollInProgress

    override val canScrollForward: Boolean get() = _value < _maxValue
    override val canScrollBackward: Boolean get() = _value > 0

    /* Take exclusive control of scrolling at the given priority and run the
       block. The ScrollScope.scrollBy(Float) calls inside dispatch through
       dispatchRawDelta — same semantics as upstream. */
    override suspend fun scroll(
        scrollPriority: MutatePriority,
        block: suspend ScrollScope.() -> Unit,
    ) {
        scrollMutex.mutateWith(scrollScope, scrollPriority) {
            _isScrollInProgress = true
            try {
                block()
            } finally {
                _isScrollInProgress = false
            }
        }
    }

    /* Bypass-the-mutex push of a pixel delta. Returns the delta actually
       consumed after clamping at edges. */
    override fun dispatchRawDelta(delta: Float): Float {
        val vOldValue = _value
        val vNewValue = (vOldValue + delta).toInt().coerceIn(0, _maxValue)
        _value = vNewValue
        _animTarget = vNewValue
        return (vNewValue - vOldValue).toFloat()
    }

    /* ScrollScope view of this ScrollableState — dispatches through
       dispatchRawDelta so all the clamping / state-mutation logic stays
       in one place. */
    private val scrollScope: ScrollScope = object : ScrollScope {
        override fun scrollBy(pixels: Float): Float = dispatchRawDelta(pixels)
    }

    /* Viewport length along the scroll axis, in px — set by layout each frame.
       Used by a Scrollbar to size its thumb (viewport / content). */
    val viewportSize: Int get() = _viewportSize

    /* Total content length along the scroll axis = scroll range + viewport.
       Before the first layout pass (maxValue still Int.MAX_VALUE) this is just
       the viewport so a Scrollbar treats the content as non-scrollable. */
    internal val contentSize: Int get() = if (_maxValue == Int.MAX_VALUE) _viewportSize else _maxValue + _viewportSize

    /* Internal: the layout sets the max + viewport each frame as content /
       viewport sizes change. */
    internal fun setMaxInternal(inMax: Int, inViewport: Int = _viewportSize) {
        val vClamped = inMax.coerceAtLeast(0)
        _maxValue = vClamped
        _viewportSize = inViewport.coerceAtLeast(0)
        if (_value > vClamped) _value = vClamped
        if (_animTarget > vClamped) _animTarget = vClamped
    }

    /* Eased scroll entry — internal so it stays out of the upstream-named
       androidx.compose.foundation public surface. The project-only extension
       `fun ScrollState.smoothScrollByPx(Int)` in
       com.compose.desktop.native.foundation wraps this and is what :window's
       mouse-wheel handler calls. */
    internal fun smoothScrollByPxInternal(inDelta: Int) {
        _animTarget = (_animTarget + inDelta).coerceIn(0, _maxValue)
        if (_animTarget != _value) ScrollAnimator.register(this)
    }

    /* One easing step toward the target; returns true while still animating.
       Called once per frame by ScrollAnimator. Moves ~half the remaining
       distance per frame and snaps the last few px so the glide ends crisply
       (≈80ms settle @60fps) instead of crawling after you stop scrolling. */
    internal fun tickSmooth(): Boolean {
        val vDiff = _animTarget - _value
        if (vDiff == 0) return false
        if (vDiff in -kSnapPx..kSnapPx) { _value = _animTarget; return false }
        val vStep = (vDiff * kSmoothFactor).roundToInt().let { if (it == 0) (if (vDiff > 0) 1 else -1) else it }
        _value = (_value + vStep).coerceIn(0, _maxValue)
        return _value != _animTarget
    }

    companion object {
        private const val kSmoothFactor = 0.5f
        private const val kSnapPx = 2
    }
}

@Composable
fun rememberScrollState(initial: Int = 0): ScrollState =
    remember { ScrollState(initial) }

// ==================
// MARK: Modifier.verticalScroll / horizontalScroll
// ==================

/* Adds vertical scrolling to the node. Children are measured with
   unbounded height, the node's own bounds clamp to incoming maxHeight,
   children are visually translated by -state.value, and the node clips
   children to its bounds. Mouse wheel events over this node call
   state.scrollBy. */
fun Modifier.verticalScroll(state: ScrollState): Modifier =
    then(VerticalScrollModifier(state))

fun Modifier.horizontalScroll(state: ScrollState): Modifier =
    then(HorizontalScrollModifier(state))
