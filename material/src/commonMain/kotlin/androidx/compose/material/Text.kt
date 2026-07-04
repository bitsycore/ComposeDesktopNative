package androidx.compose.material

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

// ==================
// MARK: Text (Material)
// ==================

/* Material `Text` matching upstream signature (color / fontSize / textAlign
   / softWrap / fontFamily are the styling knobs). `fontFamily: String?` is
   the Material Symbols-friendly form of upstream's `FontFamily?` — we
   convert to `FontFamily.Named(name)` and thread through `TextStyle`. */
@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
    fontSize: TextUnit = 16.sp,
    textAlign: TextAlign = TextAlign.Start,
    softWrap: Boolean = true,
    fontFamily: String? = null,
) {
    BasicText(
        text = text,
        modifier = modifier,
        style = TextStyle(
            color = color,
            fontSize = fontSize,
            textAlign = textAlign,
            fontFamily = fontFamily?.let { FontFamily.Named(it) },
        ),
        softWrap = softWrap,
    )
}

// ==================
// MARK: Text(AnnotatedString)
// ==================

/* Renders an AnnotatedString by splitting it at every span boundary and
   composing each contiguous-style run as its own BasicText, arranged
   horizontally. Each run picks up the SpanStyle that covers it (last
   span wins on overlap), falling back to the default `color` /
   `fontSize` for unstyled regions.

   Limitation: this is single-line rendering per-line (there's no
   per-glyph layout that can break a run across lines). For multi-line
   styled text, use explicit '\n' between Text(AnnotatedString) calls or
   stick to the plain Text(String) overload.

   Decoration (underline / line-through) is drawn via drawBehind under
   each run; background tint is applied via Modifier.background. */
@Composable
fun Text(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onBackground,
    fontSize: TextUnit = 16.sp,
    textAlign: TextAlign = TextAlign.Start,
    softWrap: Boolean = true,
    fontFamily: String? = null,
) {
    // Fast path: when every span is colour-only (the common case — syntax
    // highlighting), render the whole thing as ONE BasicText.
    val vColorOnly = text.spanStyles.all { vR ->
        val vS = vR.item
        !vS.fontSize.isSpecified && vS.fontWeight == null && vS.fontStyle == null &&
            vS.fontFamily == null && (vS.textDecoration == null || vS.textDecoration == TextDecoration.None) &&
            vS.background == Color.Unspecified && !vS.letterSpacing.isSpecified
    }
    val vBaseStyle = TextStyle(
        color = color,
        fontSize = fontSize,
        textAlign = textAlign,
        fontFamily = fontFamily?.let { FontFamily.Named(it) },
    )
    if (vColorOnly) {
        BasicText(
            text = text,
            modifier = modifier,
            style = vBaseStyle,
            softWrap = softWrap,
        )
        return
    }

    // Per-span size / weight / family / background / decoration path — lay out
    // each run separately.
    val vLines = splitIntoRunLines(text)
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        for (vLine in vLines) {
            Row {
                if (vLine.isEmpty()) {
                    // Empty line still needs to take the line-height — emit a
                    // single-space BasicText so the row doesn't collapse to 0px.
                    BasicText(text = " ", style = vBaseStyle, softWrap = false)
                } else for (vRun in vLine) {
                    val vStyle = vRun.style
                    val vColor = if (vStyle != null && vStyle.color != Color.Unspecified) vStyle.color else color
                    val vSize = if (vStyle != null && vStyle.fontSize.isSpecified) vStyle.fontSize else fontSize
                    val vFamily = if (vStyle?.fontFamily != null) vStyle.fontFamily else fontFamily?.let { FontFamily.Named(it) }
                    val vBgModifier = if (vStyle != null && vStyle.background != Color.Unspecified)
                        Modifier.background(vStyle.background) else Modifier
                    val vDec: TextDecoration? = vStyle?.textDecoration
                    val vDecorModifier = if (vDec != null && vDec != TextDecoration.None) {
                        val vDecColor = vColor
                        val vHasUnder = TextDecoration.Underline in vDec
                        val vHasLine = TextDecoration.LineThrough in vDec
                        Modifier.drawBehind {
                            if (vHasUnder) {
                                drawLine(
                                    color = vDecColor,
                                    start = Offset(0f, size.height - 2f),
                                    end = Offset(size.width, size.height - 2f),
                                    strokeWidth = 1f,
                                )
                            }
                            if (vHasLine) {
                                drawLine(
                                    color = vDecColor,
                                    start = Offset(0f, size.height / 2f),
                                    end = Offset(size.width, size.height / 2f),
                                    strokeWidth = 1f,
                                )
                            }
                        }
                    } else Modifier
                    BasicText(
                        text = vRun.text,
                        modifier = vBgModifier.then(vDecorModifier),
                        style = TextStyle(
                            color = vColor,
                            fontSize = vSize,
                            textAlign = textAlign,
                            fontFamily = vFamily,
                            fontWeight = vStyle?.fontWeight,
                            fontStyle = vStyle?.fontStyle,
                        ),
                        softWrap = false,
                    )
                }
            }
        }
    }
}

/* Split into lines of runs. First we cut the AnnotatedString at every
   '\n' (newlines aren't run-bearing — they end a Row), then within each
   line we re-split at span boundaries so each in-line run carries the
   right SpanStyle. Result is List<line> of List<run>. */
private fun splitIntoRunLines(inText: AnnotatedString): List<List<Run>> {
    if (inText.length == 0) return listOf(emptyList())
    val vOut = mutableListOf<List<Run>>()
    var vLineStart = 0
    for (vI in inText.text.indices) {
        if (inText.text[vI] == '\n') {
            vOut.add(slicedRuns(inText, vLineStart, vI))
            vLineStart = vI + 1
        }
    }
    vOut.add(slicedRuns(inText, vLineStart, inText.length))
    return vOut
}

/* Run list for inText[inStart, inEnd) — same algorithm as splitIntoRuns
   but limited to a sub-range and clipping span boundaries to that range. */
private fun slicedRuns(inText: AnnotatedString, inStart: Int, inEnd: Int): List<Run> {
    if (inStart >= inEnd) return emptyList()
    if (inText.spanStyles.isEmpty()) return listOf(Run(inText.text.substring(inStart, inEnd), null))
    val vSet = mutableSetOf(inStart, inEnd)
    for (vR in inText.spanStyles) {
        val vS = vR.start.coerceIn(inStart, inEnd)
        val vE = vR.end.coerceIn(inStart, inEnd)
        if (vS < vE) { vSet.add(vS); vSet.add(vE) }
    }
    val vSorted = vSet.toList().sorted()
    val vOut = mutableListOf<Run>()
    for (vI in 0 until vSorted.size - 1) {
        val vS = vSorted[vI]; val vE = vSorted[vI + 1]
        if (vS == vE) continue
        var vActive: SpanStyle? = null
        for (vR in inText.spanStyles) {
            if (vR.start <= vS && vR.end >= vE) vActive = vR.item
        }
        vOut.add(Run(inText.text.substring(vS, vE), vActive))
    }
    return vOut
}

/* One contiguous-style segment of an AnnotatedString. */
private data class Run(val text: String, val style: SpanStyle?)
