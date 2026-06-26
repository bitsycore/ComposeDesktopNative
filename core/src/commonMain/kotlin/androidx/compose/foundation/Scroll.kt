package androidx.compose.foundation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.HorizontalScrollModifier
import androidx.compose.ui.Modifier
import androidx.compose.ui.VerticalScrollModifier

// ==================
// MARK: ScrollState
// ==================

/* Holds a scroll offset and its currently-known max. Both are MutableState
   so any composable reading value / maxValue recomposes when they change.
   The owning Modifier.verticalScroll (or horizontalScroll) sets maxValue
   from the actual content height / width during layout. */
class ScrollState(initial: Int = 0) {
    private var _value by mutableStateOf(initial.coerceAtLeast(0))
    private var _maxValue by mutableStateOf(Int.MAX_VALUE)
    private var _viewportSize by mutableStateOf(0)

    val value: Int get() = _value
    val maxValue: Int get() = _maxValue

    /* Viewport length along the scroll axis, in px — set by layout each frame.
       Used by a Scrollbar to size its thumb (viewport / content). */
    val viewportSize: Int get() = _viewportSize

    /* Total content length along the scroll axis = scroll range + viewport.
       Before the first layout pass (maxValue still Int.MAX_VALUE) this is just
       the viewport so a Scrollbar treats the content as non-scrollable. */
    val contentSize: Int get() = if (_maxValue == Int.MAX_VALUE) _viewportSize else _maxValue + _viewportSize

    /* Internal: the layout sets the max + viewport each frame as content /
       viewport sizes change. */
    fun setMaxInternal(inMax: Int, inViewport: Int = _viewportSize) {
        val vClamped = inMax.coerceAtLeast(0)
        _maxValue = vClamped
        _viewportSize = inViewport.coerceAtLeast(0)
        if (_value > vClamped) _value = vClamped
    }

    fun scrollBy(inDelta: Int) {
        _value = (_value + inDelta).coerceIn(0, _maxValue)
    }

    fun scrollTo(inPosition: Int) {
        _value = inPosition.coerceIn(0, _maxValue)
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
