package androidx.compose.foundation.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

// ==================
// MARK: PaddingValues
// ==================

/* Per-edge padding amounts, matching androidx.compose.foundation.layout
   .PaddingValues. Modeled as an interface (not a data class) so the left/right
   edges resolve from start/end per layout direction, exactly like official. */
interface PaddingValues {
    fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp
    fun calculateTopPadding(): Dp
    fun calculateRightPadding(layoutDirection: LayoutDirection): Dp
    fun calculateBottomPadding(): Dp
}

/* Factory functions matching official construction: all edges, horizontal +
   vertical, or start / top / end / bottom. */
fun PaddingValues(all: Dp): PaddingValues = PaddingValuesImpl(all, all, all, all)

fun PaddingValues(horizontal: Dp = 0.dp, vertical: Dp = 0.dp): PaddingValues =
    PaddingValuesImpl(horizontal, vertical, horizontal, vertical)

fun PaddingValues(start: Dp = 0.dp, top: Dp = 0.dp, end: Dp = 0.dp, bottom: Dp = 0.dp): PaddingValues =
    PaddingValuesImpl(start, top, end, bottom)

/* start/end-relative implementation: left == start in LTR, == end in RTL. */
internal class PaddingValuesImpl(
    val start: Dp,
    val top: Dp,
    val end: Dp,
    val bottom: Dp,
) : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) start else end

    override fun calculateTopPadding() = top

    override fun calculateRightPadding(layoutDirection: LayoutDirection) =
        if (layoutDirection == LayoutDirection.Ltr) end else start

    override fun calculateBottomPadding() = bottom

    override fun equals(other: Any?): Boolean =
        other is PaddingValuesImpl && start == other.start && top == other.top &&
            end == other.end && bottom == other.bottom

    override fun hashCode(): Int {
        var result = start.hashCode()
        result = 31 * result + top.hashCode()
        result = 31 * result + end.hashCode()
        result = 31 * result + bottom.hashCode()
        return result
    }

    override fun toString() = "PaddingValues(start=$start, top=$top, end=$end, bottom=$bottom)"
}

/* Direction-aware start / end padding accessors (official top-level extensions). */
fun PaddingValues.calculateStartPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) calculateLeftPadding(layoutDirection)
    else calculateRightPadding(layoutDirection)

fun PaddingValues.calculateEndPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) calculateRightPadding(layoutDirection)
    else calculateLeftPadding(layoutDirection)
