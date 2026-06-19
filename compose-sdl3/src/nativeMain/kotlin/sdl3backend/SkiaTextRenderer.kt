package sdl3backend

import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.unit.IntSize
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Typeface

// ==================
// MARK: SkiaTextRenderer
// ==================

/* Replaces SDL3TextRenderer. Uses Skia's FontMgr to find a system sans-serif
   typeface and caches Font instances per pixel size. The TextMeasurer
   reports ascent + descent (no linegap) so layout centring matches the
   actual rendered glyph extent — same contract as before. */
class SkiaTextRenderer {

    private val fFontMgr: FontMgr = FontMgr.default

    private val fTypeface: Typeface? = pickTypeface()

    private val fFontCache = mutableMapOf<Int, Font>()

    /* Reports the VISIBLE bounding-box width (not the advance), so that the
       laid-out text box matches the rendered glyph extent — same as SDL3_ttf's
       surface width. Height stays at the typo metric (descent − ascent) so a
       button's height doesn't jitter when its label changes between text with
       and without descenders. */
    val textMeasurer: TextMeasurer = TextMeasurer { inText, inFontSize ->
        val vFont = getFont(inFontSize)
        val vBbox = vFont.measureText(inText, null)
        val vMetrics = vFont.metrics
        val vWidth = vBbox.width.toInt().coerceAtLeast(0)
        val vHeight = (vMetrics.descent - vMetrics.ascent).toInt().coerceAtLeast(1)
        IntSize(vWidth, vHeight)
    }

    fun drawText(
        inCanvas: Canvas,
        inText: String,
        inX: Float,
        inY: Float,
        inColor: ComposeColor,
        inFontSize: Int
    ) {
        val vFont = getFont(inFontSize)
        val vPaint = Paint().apply {
            color = toSkiaColor(inColor)
            isAntiAlias = true
        }
        // Skia's drawString places the pen at (x, baseline). The first glyph
        // may have a positive leftBearing, so the visible left edge sits to
        // the right of the pen. Subtract bbox.left to make the visible left
        // align with the laid-out box's left edge.
        val vBbox = vFont.measureText(inText, vPaint)
        val vPenX = inX - vBbox.left
        // Baseline is |ascent| below the box top (ascent is negative).
        val vBaseline = inY - vFont.metrics.ascent
        inCanvas.drawString(inText, vPenX, vBaseline, vFont, vPaint)
        vPaint.close()
    }

    fun destroy() {
        fFontCache.values.forEach { it.close() }
        fFontCache.clear()
        // fTypeface + fFontMgr.default are unmanaged Skia singletons; closing
        // them throws "Object is not managed in K/N runtime".
    }

    private fun getFont(inSize: Int): Font {
        fFontCache[inSize]?.let { return it }
        val vFont = Font(fTypeface, inSize.toFloat()).apply {
            isSubpixel = true
            edging = org.jetbrains.skia.FontEdging.SUBPIXEL_ANTI_ALIAS
        }
        fFontCache[inSize] = vFont
        return vFont
    }

    private fun pickTypeface(): Typeface? {
        // Common system sans-serif families, by platform. First match wins;
        // null falls back to FontMgr's "matchFamiliesStyle on nothing"
        // (system default).
        val vCandidates = listOf(
            "Helvetica Neue",
            "Helvetica",
            "Arial",
            "Segoe UI",
            "DejaVu Sans",
            "Liberation Sans",
            "Roboto"
        )
        for (name in vCandidates) {
            val vTf = fFontMgr.matchFamilyStyle(name, FontStyle.NORMAL)
            if (vTf != null) return vTf
        }
        return fFontMgr.matchFamiliesStyle(arrayOf<String?>(null), FontStyle.NORMAL)
    }
}

internal fun toSkiaColor(inC: ComposeColor): Int =
    Color.makeARGB(inC.a8, inC.r8, inC.g8, inC.b8)
