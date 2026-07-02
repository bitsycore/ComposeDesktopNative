package androidx.compose.foundation.text

// ==================
// MARK: StringHelpers native actuals
// ==================

/*
 Actuals for vendored StringHelpers.kt. Upstream's nonJvm actual calls
 StringBuilder.appendCodePoint() which is a JVM-only extension; on
 Kotlin/Native, StringBuilder holds UTF-16 code units, so we encode
 supplementary code points as surrogate pairs manually.

 findPrecedingBreak / findFollowingBreak / findCodePointOrEmojiStartBefore
 walk on code-point boundaries (skip low surrogate pairs). No ICU grapheme-
 cluster support yet — combining marks / emoji ZWJ sequences will land at
 codepoint boundaries. Good enough for basic caret navigation pending a
 proper break iterator.
*/

internal actual fun StringBuilder.appendCodePointX(codePoint: Int): StringBuilder {
	if (codePoint < 0x10000) {
		append(codePoint.toChar())
	} else {
		val vBase = codePoint - 0x10000
		append(((vBase ushr 10) + 0xD800).toChar())
		append(((vBase and 0x3FF) + 0xDC00).toChar())
	}
	return this
}

internal actual fun String.findPrecedingBreak(index: Int): Int {
	if (index <= 0) return 0
	var i = index - 1
	if (i > 0 && this[i].isLowSurrogate() && this[i - 1].isHighSurrogate()) i--
	return i
}

internal actual fun String.findFollowingBreak(index: Int): Int {
	if (index >= length) return length
	var i = index + 1
	if (i < length && this[index].isHighSurrogate() && this[i].isLowSurrogate()) i++
	return i
}

internal actual fun String.findCodePointOrEmojiStartBefore(index: Int, ifNotFound: Int): Int {
	if (index <= 0) return ifNotFound
	var i = index - 1
	if (i > 0 && this[i].isLowSurrogate() && this[i - 1].isHighSurrogate()) i--
	return i
}
