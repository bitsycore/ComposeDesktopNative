package androidx.compose.foundation.text.input.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

// ==================
// MARK: Offset.coerceIn(Rect) — extract
// ==================

/*
 Extracted from upstream `foundation.text.input.internal.TextLayoutState.kt`.
 That file has the full TextLayoutState class (~230L) that needs the whole
 BasicTextField 2 rework — TextLayoutInput, MinLinesConstrainer,
 LayoutCoordinates plumbing etc. SelectionManager only needs the
 free-standing `Offset.coerceIn(rect)` extension, so byte-identical extract.

 TODO: delete this file when TextLayoutState.kt can vendor cleanly.
*/
internal fun Offset.coerceIn(rect: Rect): Offset {
	val xOffset =
		when {
			x < rect.left -> rect.left
			x > rect.right -> rect.right
			else -> x
		}
	val yOffset =
		when {
			y < rect.top -> rect.top
			y > rect.bottom -> rect.bottom
			else -> y
		}
	return Offset(xOffset, yOffset)
}
