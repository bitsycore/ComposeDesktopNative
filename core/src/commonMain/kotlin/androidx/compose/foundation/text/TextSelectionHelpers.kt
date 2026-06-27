package androidx.compose.foundation.text

import androidx.compose.ui.text.TextRange
import kotlin.time.TimeSource

// ==================
// MARK: Multi-click selection helpers
// ==================
// Shared by BasicTextField (editable) and SelectableText/SelectionContainer
// (read-only) so double / triple click behave the same in both — like a text
// editor: 2 clicks = word, 3 clicks = line.

// Monotonic ms source for multi-click timing (distinct from BasicTextField's
// own clock so neither depends on the other).
private val kClick = TimeSource.Monotonic.markNow()
internal fun monotonicMillis(): Long = kClick.elapsedNow().inWholeMilliseconds

/* Max gap between presses (ms) and max pointer movement (px) to count as the
   same multi-click sequence. */
internal const val kMultiClickMs = 350L
internal const val kMultiClickSlopPx = 3

private fun isWordChar(inC: Char): Boolean = inC.isLetterOrDigit() || inC == '_'

/* Editor-style "word at index" as a TextRange:
   - on a word char (letter/digit/_): the whole word run.
   - on punctuation: that run of punctuation.
   - on whitespace: snap to the NEAREST word (closer side wins) and select it;
     if the text is entirely whitespace, the whitespace run.
   Used for double-click selection. */
internal fun wordRangeAt(inText: String, inIndex: Int): TextRange {
	val vN = inText.length
	if (vN == 0) return TextRange(0)
	var i = inIndex.coerceIn(0, vN)
	if (i >= vN) i = vN - 1
	if (inText[i].isWhitespace()) {
		// Nearest word char on either side; closer one wins.
		var vL = i - 1
		while (vL >= 0 && inText[vL].isWhitespace()) vL--
		var vR = i + 1
		while (vR < vN && inText[vR].isWhitespace()) vR++
		val vDl = if (vL >= 0) i - vL else Int.MAX_VALUE
		val vDr = if (vR < vN) vR - i else Int.MAX_VALUE
		if (vDl == Int.MAX_VALUE && vDr == Int.MAX_VALUE) {
			// All whitespace — select the whitespace run.
			var s = i; while (s > 0 && inText[s - 1].isWhitespace()) s--
			var e = i + 1; while (e < vN && inText[e].isWhitespace()) e++
			return TextRange(s, e)
		}
		i = if (vDl <= vDr) vL else vR
	}
	var vS = i
	var vE = i + 1
	if (isWordChar(inText[i])) {
		while (vS > 0 && isWordChar(inText[vS - 1])) vS--
		while (vE < vN && isWordChar(inText[vE])) vE++
	} else {
		// Run of punctuation (non-word, non-whitespace).
		while (vS > 0 && !isWordChar(inText[vS - 1]) && !inText[vS - 1].isWhitespace()) vS--
		while (vE < vN && !isWordChar(inText[vE]) && !inText[vE].isWhitespace()) vE++
	}
	return TextRange(vS, vE)
}

/* The logical line containing inIndex (between '\n's, newlines excluded) as a
   TextRange. Used for triple-click selection. */
internal fun lineRangeAt(inText: String, inIndex: Int): TextRange {
	val vN = inText.length
	val i = inIndex.coerceIn(0, vN)
	var vS = i
	while (vS > 0 && inText[vS - 1] != '\n') vS--
	var vE = i
	while (vE < vN && inText[vE] != '\n') vE++
	return TextRange(vS, vE)
}
