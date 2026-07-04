package androidx.compose.foundation.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.isUnspecified
import androidx.compose.ui.unit.sp

// ==================
// MARK: BasicText
// ==================

/* Byte-signature match of upstream `androidx.compose.foundation.text.BasicText`
   — `style: TextStyle` is the sole styling channel (color / fontSize /
   textAlign / fontFamily all live inside TextStyle). No non-upstream extras:
   icon-font rendering with variable-font axes (Material Symbols) uses
   `com.compose.desktop.native.text.IconText` instead, since upstream
   TextStyle doesn't model per-usage `fontVariationSettings` (variation is
   set per-Font at construction time upstream).

   TODO: swap this project impl for the vendored upstream `BasicText.kt`
   once ParagraphLayoutCache's TextLayoutResult production picks up our
   SdlParagraph via the FontFamily.Resolver bridge (see Text Phase 4).
   overflow / maxLines / minLines / onTextLayout are accept-and-ignore
   today — the project leaf doesn't produce a real TextLayoutResult.

   FontFamily.Named is the only family form the SDL renderer honours;
   generic FontFamily.SansSerif / Serif / Monospace fall through to the
   default typeface. */
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
) {
    @Suppress("UNUSED_PARAMETER") val ignoredOverflow = overflow
    @Suppress("UNUSED_PARAMETER") val ignoredMaxLines = maxLines
    @Suppress("UNUSED_PARAMETER") val ignoredMinLines = minLines
    @Suppress("UNUSED_PARAMETER") val ignoredOnTextLayout = onTextLayout
    val vResolved = resolveTextStyle(style)
    TextLeaf(text, null, modifier, vResolved.color, vResolved.fontSize, vResolved.textAlign, softWrap, vResolved.fontFamily)
}

/* AnnotatedString overload — draws `text.text` with per-span colours from
   text.spanStyles in a single text node. Spans are color-only for layout;
   the plain text drives measurement/wrap. */
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
) {
    @Suppress("UNUSED_PARAMETER") val ignoredOverflow = overflow
    @Suppress("UNUSED_PARAMETER") val ignoredMaxLines = maxLines
    @Suppress("UNUSED_PARAMETER") val ignoredMinLines = minLines
    @Suppress("UNUSED_PARAMETER") val ignoredOnTextLayout = onTextLayout
    val vResolved = resolveTextStyle(style)
    TextLeaf(text.text, text.spanStyles, modifier, vResolved.color, vResolved.fontSize, vResolved.textAlign, softWrap, vResolved.fontFamily)
}

private data class ResolvedStyle(
    val color: Color,
    val fontSize: TextUnit,
    val textAlign: TextAlign,
    val fontFamily: String?,
)

/* Resolve TextStyle → (color, fontSize, textAlign, fontFamily) for the
   text leaf. Applies project defaults for unspecified values. */
private fun resolveTextStyle(inStyle: TextStyle): ResolvedStyle {
    val vColor = if (inStyle.color == Color.Unspecified) Color.White else inStyle.color
    val vSize = if (inStyle.fontSize.isUnspecified) 14.sp else inStyle.fontSize
    val vAlign = inStyle.textAlign.let { if (it == TextAlign.Unspecified) TextAlign.Start else it }
    val vFamily = (inStyle.fontFamily as? FontFamily.Named)?.name
    return ResolvedStyle(vColor, vSize, vAlign, vFamily)
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
) {
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
                fontVariations = null,
            )
        ),
    ) { _, constraints ->
        val vWrapWidth =
            if (softWrap && constraints.maxWidth != androidx.compose.ui.unit.Constraints.Infinity) constraints.maxWidth
            else Int.MAX_VALUE
        val vSize = com.compose.desktop.native.text.currentTextMeasurer.measure(
            text, vFontPx, vWrapWidth, fontFamily, null,
        )
        val w = if (constraints.minWidth >= constraints.maxWidth) constraints.maxWidth
                else vSize.width.coerceIn(constraints.minWidth, constraints.maxWidth)
        val h = vSize.height.coerceIn(constraints.minHeight, constraints.maxHeight)
        layout(w, h) {}
    }
}
