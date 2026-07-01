@file:Suppress("DEPRECATION")

package androidx.compose.foundation.layout

import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==================
// MARK: ContextualFlowLayout — non-Subcompose subset (extracted shim)
// ==================

/**
 * Extracted subset of upstream `ContextualFlowLayout.kt`. Vendored
 * `FlowLayout.kt` + `FlowLayoutOverflow.kt` reference these six types
 * for their `getNext` / `update` / lazy-iterator paths — but the rest
 * of `ContextualFlowLayout.kt` requires `SubcomposeLayout` /
 * `SubcomposeMeasureScope`, which our project doesn't vendor yet.
 *
 * Each declaration below is copied verbatim from upstream (no shape
 * change) — this shim retires when SubcomposeLayout lands and the
 * full `ContextualFlowLayout.kt` is vendored.
 */

/** Scope for the overflow `ContextualFlowRow`. */
@Deprecated("ContextualFlowLayouts are no longer maintained")
@LayoutScopeMarker
@Stable
@ExperimentalLayoutApi
interface ContextualFlowRowOverflowScope : FlowRowOverflowScope

/** Scope for the overflow `ContextualFlowColumn`. */
@Deprecated("ContextualFlowLayouts are no longer maintained")
@LayoutScopeMarker
@Stable
@ExperimentalLayoutApi
interface ContextualFlowColumnOverflowScope : FlowColumnOverflowScope

@ExperimentalLayoutApi
internal class ContextualFlowRowOverflowScopeImpl(private val state: FlowLayoutOverflowState) :
    FlowRowOverflowScope by FlowRowOverflowScopeImpl(state), ContextualFlowRowOverflowScope

@ExperimentalLayoutApi
internal class ContextualFlowColumnOverflowScopeImpl(private val state: FlowLayoutOverflowState) :
    FlowColumnOverflowScope by FlowColumnOverflowScopeImpl(state), ContextualFlowColumnOverflowScope

internal class ContextualFlowItemIterator(
    private val itemCount: Int,
    private val getMeasurables: (index: Int, info: FlowLineInfo) -> List<Measurable>,
) : Iterator<Measurable> {
    private val _list: MutableList<Measurable> = mutableListOf()
    private var itemIndex: Int = 0
    private var listIndex = 0
    val list: List<Measurable>
        get() = _list

    override fun hasNext(): Boolean {
        return listIndex < list.size || itemIndex < itemCount
    }

    override fun next(): Measurable {
        return getNext()
    }

    internal fun getNext(info: FlowLineInfo = FlowLineInfo()): Measurable {
        return if (listIndex < list.size) {
            val measurable = list[listIndex]
            listIndex++
            measurable
        } else if (itemIndex < itemCount) {
            val measurables = getMeasurables(itemIndex, info)
            itemIndex++
            if (measurables.isEmpty()) {
                next()
            } else {
                val measurable = measurables.first()
                _list.addAll(measurables)
                listIndex++
                measurable
            }
        } else {
            throw IndexOutOfBoundsException("No item returned at index call. Index: $itemIndex")
        }
    }
}

/**
 * Contextual Line Info for the current lazy call for `ContextualFlowRow` or
 * `ContextualFlowColumn`.
 */
internal class FlowLineInfo(
    internal var lineIndex: Int = 0,
    internal var positionInLine: Int = 0,
    internal var maxMainAxisSize: Dp = 0.dp,
    internal var maxCrossAxisSize: Dp = 0.dp,
) {

    /** To allow reuse of the same object to reduce allocation, simply update the same value */
    internal fun update(
        lineIndex: Int,
        positionInLine: Int,
        maxMainAxisSize: Dp,
        maxCrossAxisSize: Dp,
    ) {
        this.lineIndex = lineIndex
        this.positionInLine = positionInLine
        this.maxMainAxisSize = maxMainAxisSize
        this.maxCrossAxisSize = maxCrossAxisSize
    }
}
