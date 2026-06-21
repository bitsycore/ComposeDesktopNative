package com.compose.desktop.native.renderer.skia

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.RadialGradient
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.SweepGradient
import androidx.compose.ui.graphics.TileMode as ComposeTileMode
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.StrokeCap as ComposeStrokeCap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.Color
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.PaintStrokeCap
import org.jetbrains.skia.Rect

// ==================
// MARK: SkiaDrawScope
// ==================

/* DrawScope implementation that translates the common-side primitives into
   native Skia Canvas calls. Each method allocates a Paint, configures
   fill / stroke style + shader, draws, then closes the Paint — Skia's
   ref-counting frees the underlying SkPaint promptly. */
internal class SkiaDrawScope(
	private val fCanvas: Canvas,
	private val fOriginX: Float,
	private val fOriginY: Float,
	override val size: Size,
) : DrawScope {

	override fun drawRect(
		brush: Brush,
		topLeft: Offset,
		size: Size,
		alpha: Float,
		style: DrawStyle,
	) {
		val vPaint = paintFor(brush, alpha, style, size)
		val vRect = Rect.makeXYWH(
			fOriginX + topLeft.x,
			fOriginY + topLeft.y,
			size.width,
			size.height,
		)
		fCanvas.drawRect(vRect, vPaint)
		vPaint.close()
	}

	override fun drawCircle(
		brush: Brush,
		radius: Float,
		center: Offset,
		alpha: Float,
		style: DrawStyle,
	) {
		val vPaint = paintFor(brush, alpha, style, Size(radius * 2f, radius * 2f))
		fCanvas.drawCircle(
			fOriginX + center.x,
			fOriginY + center.y,
			radius,
			vPaint,
		)
		vPaint.close()
	}

	override fun drawArc(
		brush: Brush,
		startAngle: Float,
		sweepAngle: Float,
		useCenter: Boolean,
		topLeft: Offset,
		size: Size,
		alpha: Float,
		style: DrawStyle,
	) {
		val vPaint = paintFor(brush, alpha, style, size)
		val vRect = Rect.makeXYWH(
			fOriginX + topLeft.x,
			fOriginY + topLeft.y,
			size.width,
			size.height,
		)
		fCanvas.drawArc(
			vRect.left, vRect.top, vRect.right, vRect.bottom,
			startAngle, sweepAngle, useCenter, vPaint,
		)
		vPaint.close()
	}

	override fun drawLine(
		brush: Brush,
		start: Offset,
		end: Offset,
		strokeWidth: Float,
		cap: ComposeStrokeCap,
		alpha: Float,
	) {
		val vPaint = paintFor(brush, alpha, Stroke(strokeWidth, cap), Size(0f, 0f))
		fCanvas.drawLine(
			fOriginX + start.x, fOriginY + start.y,
			fOriginX + end.x, fOriginY + end.y,
			vPaint,
		)
		vPaint.close()
	}

	// ============
	//  Paint factory — wires colour / brush / style / cap onto a fresh
	//  Skia Paint. inShapeSize is used to resolve gradient anchor points
	//  that the caller left at Float.POSITIVE_INFINITY ("full bounds").

	private fun paintFor(
		inBrush: Brush,
		inAlpha: Float,
		inStyle: DrawStyle,
		inShapeSize: Size,
	): Paint {
		val vPaint = Paint().apply { isAntiAlias = true }
		when (inStyle) {
			Fill -> vPaint.mode = PaintMode.FILL
			is Stroke -> {
				vPaint.mode = PaintMode.STROKE
				vPaint.strokeWidth = inStyle.width
				vPaint.strokeCap = when (inStyle.cap) {
					ComposeStrokeCap.Butt -> PaintStrokeCap.BUTT
					ComposeStrokeCap.Round -> PaintStrokeCap.ROUND
					ComposeStrokeCap.Square -> PaintStrokeCap.SQUARE
				}
			}
		}
		when (inBrush) {
			is SolidColor -> vPaint.color = inBrush.color.withAlphaScaled(inAlpha).toSkiaColor()
			is LinearGradient -> {
				// TODO: wire to Skia's Gradient/Shader API for 0.150. For now
				// degrade to the gradient's first colour so widgets that
				// request a gradient still draw something sensible.
				vPaint.color = (inBrush.colors.firstOrNull() ?: ComposeColor.Transparent)
					.withAlphaScaled(inAlpha).toSkiaColor()
			}
			is RadialGradient -> {
				vPaint.color = (inBrush.colors.firstOrNull() ?: ComposeColor.Transparent)
					.withAlphaScaled(inAlpha).toSkiaColor()
			}
			is SweepGradient -> {
				vPaint.color = (inBrush.colors.firstOrNull() ?: ComposeColor.Transparent)
					.withAlphaScaled(inAlpha).toSkiaColor()
			}
		}
		return vPaint
	}
}

// ==================
// MARK: Helpers
// ==================

private fun ComposeColor.toSkiaColor(): Int =
	Color.makeARGB(a8, r8, g8, b8)

private fun ComposeColor.withAlphaScaled(inAlpha: Float): ComposeColor =
	if (inAlpha >= 1f) this else copy(alpha = alpha * inAlpha)
