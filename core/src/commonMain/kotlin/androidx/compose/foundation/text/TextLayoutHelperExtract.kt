package androidx.compose.foundation.text

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextRange

// ==================
// MARK: TextLayoutHelper — extracts
// ==================

/*
 Extracted from upstream foundation.text.TextLayoutHelper.kt. The full file
 has a `canReuse(...)` extension that needs `FontFamily.Resolver`
 (unvendored — the font-family resolver stack routes through platform
 typeface loaders we don't host). MultiWidgetSelectionDelegate needs
 `getLineHeight`, and SelectionManager needs `isPositionInsideSelection`.
 These are byte-identical extracts.

 TODO: delete this file when TextLayoutHelper.kt can vendor cleanly.
*/
internal fun TextLayoutResult.getLineHeight(offset: Int): Float {
	if (offset < 0 || layoutInput.text.isEmpty()) return 0f

	val line =
		minOf(
			multiParagraph.getLineForOffset(offset),
			multiParagraph.maxLines - 1,
			multiParagraph.lineCount - 1,
		)
	val lineEnd = multiParagraph.getLineEnd(line)
	if (offset > lineEnd) return 0f

	return multiParagraph.getLineHeight(line)
}

/** Returns whether the given pixel position is inside the selection. */
internal fun TextLayoutResult.isPositionInsideSelection(
	position: Offset,
	selectionRange: TextRange?,
): Boolean {
	if ((selectionRange == null) || selectionRange.collapsed) return false

	fun isOffsetSelectedAndContainsPosition(offset: Int) =
		selectionRange.contains(offset) && getBoundingBox(offset).contains(position)

	val offset = getOffsetForPosition(position)
	return isOffsetSelectedAndContainsPosition(offset) ||
		isOffsetSelectedAndContainsPosition(offset - 1)
}
