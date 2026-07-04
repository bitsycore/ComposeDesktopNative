package androidx.compose.foundation.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

// ==================
// MARK: BasicText
// ==================

/* style / softWrap / overflow / maxLines / minLines / onTextLayout / color
   mirror upstream's signature. fontFamily / fontVariationSettings are
   documented non-upstream additions the renderer needs for icon-font
   variable-axis support (upstream's FontFamily.Resolver routes through
   platform typeface loaders we don't host).

   TODO(BasicText Phase 4): swap this project impl for the vendored
   upstream `foundation.text.BasicText.kt` once the vendored
   modifier chain (TextStringSimpleElement +
   TextAnnotatedStringElement) has a real FontFamily.Resolver +
   LocalSelectionRegistrar + LocalTextSelectionColors + a
   TextLayoutResult-producing text leaf. Right now our leaf can't
   produce a TextLayoutResult, so overflow/maxLines/minLines/onTextLayout
   are accept-and-ignore. */
@Composable
fun BasicText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    fontFamily: String? = null,
    fontVariationSettings: List<FontVariation.Setting>? = null,
) {
    @Suppress("UNUSED_PARAMETER") val ignoredOverflow = overflow
    @Suppress("UNUSED_PARAMETER") val ignoredMaxLines = maxLines
    @Suppress("UNUSED_PARAMETER") val ignoredMinLines = minLines
    @Suppress("UNUSED_PARAMETER") val ignoredOnTextLayout = onTextLayout
    val (vColor, vSize, vAlign) = resolveTextStyle(style)
    TextLeaf(text, null, modifier, vColor, vSize, vAlign, softWrap, fontFamily, fontVariationSettings)
}

/* AnnotatedString overload — draws `text.text` with per-span colours
   (text.spanStyles) in a single text node. Spans are color-only for layout:
   the plain text drives measurement/wrap, so this is safe to use as the
   display layer of an editable field (cursor / selection map to the plain
   text). Per-span weight / decoration aren't applied here — for those use the
   Material Text(AnnotatedString) overload, which lays out per-run. */
@Composable
fun BasicText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    fontFamily: String? = null,
    fontVariationSettings: List<FontVariation.Setting>? = null,
) {
    @Suppress("UNUSED_PARAMETER") val ignoredOverflow = overflow
    @Suppress("UNUSED_PARAMETER") val ignoredMaxLines = maxLines
    @Suppress("UNUSED_PARAMETER") val ignoredMinLines = minLines
    @Suppress("UNUSED_PARAMETER") val ignoredOnTextLayout = onTextLayout
    val (vColor, vSize, vAlign) = resolveTextStyle(style)
    TextLeaf(text.text, text.spanStyles, modifier, vColor, vSize, vAlign, softWrap, fontFamily, fontVariationSettings)
}

/* Resolve TextStyle to the (color, fontSize, textAlign) triple our text node
   accepts. Color.Unspecified → Color.White; TextUnit.Unspecified → 14.sp
   (matching upstream's defaults); null TextAlign → TextAlign.Start. */
private fun resolveTextStyle(inStyle: TextStyle): Triple<Color, TextUnit, TextAlign> {
    val vColor = if (inStyle.color == Color.Unspecified) Color.White else inStyle.color
    val vSize = if (inStyle.fontSize.isUnspecified) 14.sp else inStyle.fontSize
    val vAlign = inStyle.textAlign ?: TextAlign.Start
    return Triple(vColor, vSize, vAlign)
}

/* The text leaf node — defers measurement + drawing to the installed renderer. */
@Composable
private fun TextLeaf(
    text: String,
    spans: List<Range<SpanStyle>>?,
    modifier: Modifier,
    color: Color,
    fontSize: TextUnit,
    textAlign: TextAlign,
    softWrap: Boolean,
    fontFamily: String?,
    fontVariationSettings: List<FontVariation.Setting>?,
) {
    // Phase 9 B5: build an upstream LayoutNode via the vendored Layout — sized by the
    // installed TextMeasurer, drawn by a TextDrawNode (DrawModifierNode) that bridges
    // to the renderer's native text drawing. Text is a real draw node in the chain.
    val vFontPx = fontSize.value.toInt()
    androidx.compose.ui.layout.Layout(
        modifier = modifier.then(
            com.compose.desktop.native.text.TextDrawElement(
                text = text,
                spans = spans,
                color = color,
                fontSizePx = vFontPx,
                textAlign = textAlign,
                softWrap = softWrap,
                fontFamily = fontFamily,
                fontVariations = fontVariationSettings,
            )
        ),
    ) { _, constraints ->
        val vWrapWidth =
            if (softWrap && constraints.maxWidth != androidx.compose.ui.unit.Constraints.Infinity) constraints.maxWidth
            else Int.MAX_VALUE
        val vSize = com.compose.desktop.native.text.currentTextMeasurer.measure(
            text, vFontPx, vWrapWidth, fontFamily, fontVariationSettings,
        )
        val w = if (constraints.minWidth >= constraints.maxWidth) constraints.maxWidth
                else vSize.width.coerceIn(constraints.minWidth, constraints.maxWidth)
        val h = vSize.height.coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(w, h) {}
    }
}
