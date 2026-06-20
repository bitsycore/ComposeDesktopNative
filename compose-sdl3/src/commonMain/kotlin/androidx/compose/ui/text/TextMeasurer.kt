package androidx.compose.ui.text

import androidx.compose.ui.unit.IntSize

// ==================
// MARK: TextMeasurer
// ==================

/* Shared abstraction so that the commonMain layout pass (TextMeasurePolicy) can
   get the same width/height that the native renderer will actually draw. The
   native backend installs a Skia-backed implementation at startup. */
interface TextMeasurer {
	/* Measure the text's laid-out size. If inMaxWidth is bounded, lines wrap
	   at word boundaries (or mid-word if a single word exceeds the limit). */
	fun measure(inText: String, inFontSize: Int, inMaxWidth: Int = Int.MAX_VALUE): IntSize

	/* Wrapped lines for the same parameters. Same algorithm as measure;
	   exposed so callers (BasicTextField, renderer) can iterate the same
	   line breakdown without re-measuring. */
	fun wrap(inText: String, inFontSize: Int, inMaxWidth: Int = Int.MAX_VALUE): List<String>
}

// ==================
// MARK: Default fallback
// ==================

private val kFallbackTextMeasurer = object : TextMeasurer {
	override fun measure(inText: String, inFontSize: Int, inMaxWidth: Int): IntSize {
		val vCharW = (inFontSize * 0.6f).toInt().coerceAtLeast(1)
		return IntSize(vCharW * inText.length, (inFontSize * 1.3f).toInt())
	}
	override fun wrap(inText: String, inFontSize: Int, inMaxWidth: Int): List<String> =
		inText.split('\n')
}

var currentTextMeasurer: TextMeasurer = kFallbackTextMeasurer
