package androidx.compose.foundation.layout

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==================
// MARK: PaddingValues
// ==================

/* Padding amounts for the four edges, matching
   androidx.compose.foundation.layout.PaddingValues. Official models this as an
   interface with calculate{Left,Top,Right,Bottom}Padding(); this project ships a
   reduced data class exposing the edge Dp values directly until that reshape
   lands. Construction (all / horizontal+vertical / start,top,end,bottom) matches
   the official factory functions. */
data class PaddingValues(
    val start: Dp = 0.dp,
    val top: Dp = 0.dp,
    val end: Dp = 0.dp,
    val bottom: Dp = 0.dp
) {
    constructor(all: Dp) : this(all, all, all, all)
    constructor(horizontal: Dp = 0.dp, vertical: Dp = 0.dp)
        : this(horizontal, vertical, horizontal, vertical)
}
