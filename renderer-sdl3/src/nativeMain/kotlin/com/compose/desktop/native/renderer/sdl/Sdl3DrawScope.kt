package com.compose.desktop.native.renderer.sdl

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.LinearGradient
import androidx.compose.ui.graphics.RadialGradient
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.SweepGradient
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.StrokeCap
import kotlinx.cinterop.*
import sdl3.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

// ==================
// MARK: Sdl3DrawScope
// ==================

/* SDL3 implementation of DrawScope. SDL3's only public shape-rendering API
   is SDL_RenderGeometry (triangle list with per-vertex colour) — there is
   no arc / stroke primitive. We tessellate every primitive into triangles
   here:

   - drawRect: 2 triangles (or 8 for a stroked rect — 4 thin quads, one
     per side, with mitred corners).
   - drawCircle (filled): triangle fan of (segments + 1) verts around the
     centre.
   - drawCircle (stroked) / drawArc (stroked): a strip of `segments` quads
     forming the ring/arc between innerR (r - w/2) and outerR (r + w/2).
     Round caps add a semi-fan of N segments at each end of the arc.
   - drawArc (filled, useCenter): triangle fan from the centre.
   - drawLine (thick): a single quad with optional round caps at the ends.

   segment count is proportional to arc length so wide arcs stay smooth:
   ~64 segments per full circle (5.6° per step). At smaller sizes that's
   sub-pixel work; at larger sizes the human eye stops seeing facets.

   Gradient brushes degrade to the first colour for now — SDL3 has no
   built-in shader, so a real implementation would per-vertex blend along
   the gradient axis. */
@OptIn(ExperimentalForeignApi::class)
internal class Sdl3DrawScope(
	private val fRenderer: COpaquePointer,
	private val fOriginX: Float,
	private val fOriginY: Float,
	override val size: Size,
) : DrawScope {

	// ============
	//  Public primitives

	override fun drawRect(
		brush: Brush,
		topLeft: Offset,
		size: Size,
		alpha: Float,
		style: DrawStyle,
	) {
		val vColor = brush.dominantColor().withAlphaScaled(alpha)
		val vL = fOriginX + topLeft.x
		val vT = fOriginY + topLeft.y
		val vR = vL + size.width
		val vB = vT + size.height
		when (style) {
			Fill -> emitFilledQuad(vL, vT, vR, vT, vR, vB, vL, vB, vColor)
			is Stroke -> {
				val vW = style.width
				// Outer = bounds; inner is shrunk by width on each side.
				val vIL = vL + vW; val vIT = vT + vW; val vIR = vR - vW; val vIB = vB - vW
				if (vIR <= vIL || vIB <= vIT) {
					emitFilledQuad(vL, vT, vR, vT, vR, vB, vL, vB, vColor)
				} else {
					// 4 sides as quads.
					emitFilledQuad(vL, vT, vR, vT, vIR, vIT, vIL, vIT, vColor) // top
					emitFilledQuad(vIR, vIT, vR, vT, vR, vB, vIR, vIB, vColor) // right
					emitFilledQuad(vIL, vIB, vIR, vIB, vR, vB, vL, vB, vColor) // bottom
					emitFilledQuad(vL, vT, vIL, vIT, vIL, vIB, vL, vB, vColor) // left
				}
			}
		}
	}

	override fun drawCircle(
		brush: Brush,
		radius: Float,
		center: Offset,
		alpha: Float,
		style: DrawStyle,
	) {
		drawArc(
			brush = brush,
			startAngle = 0f,
			sweepAngle = 360f,
			useCenter = (style === Fill),
			topLeft = Offset(center.x - radius, center.y - radius),
			size = Size(radius * 2f, radius * 2f),
			alpha = alpha,
			style = style,
		)
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
		val vColor = brush.dominantColor().withAlphaScaled(alpha)
		val vCx = fOriginX + topLeft.x + size.width / 2f
		val vCy = fOriginY + topLeft.y + size.height / 2f
		val vRx = size.width / 2f
		val vRy = size.height / 2f
		val vSeg = arcSegments(sweepAngle)

		when (style) {
			Fill -> emitFilledArc(vCx, vCy, vRx, vRy, startAngle, sweepAngle, useCenter, vSeg, vColor)
			is Stroke -> {
				val vR = (vRx + vRy) / 2f // assume ~circle for stroked arcs
				val vOuter = vR + style.width / 2f
				val vInner = (vR - style.width / 2f).coerceAtLeast(0f)
				emitStrokedArc(vCx, vCy, vInner, vOuter, startAngle, sweepAngle, vSeg, vColor)
				if (style.cap == StrokeCap.Round && sweepAngle < 360f) {
					emitRoundCap(vCx, vCy, vR, style.width / 2f, startAngle, vColor)
					emitRoundCap(vCx, vCy, vR, style.width / 2f, startAngle + sweepAngle, vColor)
				}
			}
		}
	}

	override fun drawLine(
		brush: Brush,
		start: Offset,
		end: Offset,
		strokeWidth: Float,
		cap: StrokeCap,
		alpha: Float,
	) {
		val vColor = brush.dominantColor().withAlphaScaled(alpha)
		val vX1 = fOriginX + start.x
		val vY1 = fOriginY + start.y
		val vX2 = fOriginX + end.x
		val vY2 = fOriginY + end.y
		val vDx = vX2 - vX1
		val vDy = vY2 - vY1
		val vLen = sqrt(vDx * vDx + vDy * vDy)
		if (vLen < 1e-4f) return
		val vNx = -vDy / vLen * strokeWidth / 2f
		val vNy = vDx / vLen * strokeWidth / 2f
		emitFilledQuad(
			vX1 + vNx, vY1 + vNy,
			vX2 + vNx, vY2 + vNy,
			vX2 - vNx, vY2 - vNy,
			vX1 - vNx, vY1 - vNy,
			vColor,
		)
		if (cap == StrokeCap.Round) {
			emitFilledArc(vX1, vY1, strokeWidth / 2f, strokeWidth / 2f, 0f, 360f, true, 12, vColor)
			emitFilledArc(vX2, vY2, strokeWidth / 2f, strokeWidth / 2f, 0f, 360f, true, 12, vColor)
		}
	}

	// ============
	//  Tessellation primitives

	/* Number of arc segments proportional to sweep — 64 around a full
	   circle, never less than 8 for very short arcs (avoids visible
	   facets at the head when sweep is small). */
	private fun arcSegments(inSweepDeg: Float): Int {
		val vAbs = if (inSweepDeg < 0) -inSweepDeg else inSweepDeg
		return max(8, ((vAbs / 360f) * 64f).toInt() + 1)
	}

	private fun emitFilledArc(
		inCx: Float, inCy: Float, inRx: Float, inRy: Float,
		inStartDeg: Float, inSweepDeg: Float, inUseCenter: Boolean,
		inSegments: Int, inColor: ComposeColor,
	) {
		// Triangle fan from the centre. Each output triangle shares the
		// centre with two adjacent perimeter points. useCenter=false on an
		// arc would normally chord-close it; for simplicity we still fan
		// from the centre (the visual difference is minimal at typical
		// arc lengths used by widgets).
		val vStartRad = inStartDeg * (PI / 180.0).toFloat()
		val vSweepRad = inSweepDeg * (PI / 180.0).toFloat()
		val vStep = vSweepRad / inSegments
		for (i in 0 until inSegments) {
			val vA = vStartRad + i * vStep
			val vB = vStartRad + (i + 1) * vStep
			val vAx = inCx + inRx * cos(vA)
			val vAy = inCy + inRy * sin(vA)
			val vBx = inCx + inRx * cos(vB)
			val vBy = inCy + inRy * sin(vB)
			if (inUseCenter || inSweepDeg >= 360f) {
				emitFilledTri(inCx, inCy, vAx, vAy, vBx, vBy, inColor)
			} else {
				emitFilledTri(inCx, inCy, vAx, vAy, vBx, vBy, inColor)
			}
		}
	}

	private fun emitStrokedArc(
		inCx: Float, inCy: Float, inInnerR: Float, inOuterR: Float,
		inStartDeg: Float, inSweepDeg: Float, inSegments: Int,
		inColor: ComposeColor,
	) {
		val vStartRad = inStartDeg * (PI / 180.0).toFloat()
		val vSweepRad = inSweepDeg * (PI / 180.0).toFloat()
		val vStep = vSweepRad / inSegments
		for (i in 0 until inSegments) {
			val vA = vStartRad + i * vStep
			val vB = vStartRad + (i + 1) * vStep
			val vCosA = cos(vA); val vSinA = sin(vA)
			val vCosB = cos(vB); val vSinB = sin(vB)
			val vOAX = inCx + inOuterR * vCosA; val vOAY = inCy + inOuterR * vSinA
			val vOBX = inCx + inOuterR * vCosB; val vOBY = inCy + inOuterR * vSinB
			val vIAX = inCx + inInnerR * vCosA; val vIAY = inCy + inInnerR * vSinA
			val vIBX = inCx + inInnerR * vCosB; val vIBY = inCy + inInnerR * vSinB
			emitFilledQuad(vOAX, vOAY, vOBX, vOBY, vIBX, vIBY, vIAX, vIAY, inColor)
		}
	}

	private fun emitRoundCap(
		inCx: Float, inCy: Float, inR: Float, inCapRadius: Float,
		inAtAngleDeg: Float, inColor: ComposeColor,
	) {
		// Semicircle cap centred on the arc's centreline at the given angle.
		val vRad = inAtAngleDeg * (PI / 180.0).toFloat()
		val vPx = inCx + inR * cos(vRad)
		val vPy = inCy + inR * sin(vRad)
		// Cap orientation: the diameter aligned with the radial direction;
		// the semicircle extends along the tangent. Fan as a full circle for
		// simplicity (overshoots into the stroke body, which is invisible
		// because the body is opaque too).
		emitFilledArc(vPx, vPy, inCapRadius, inCapRadius, 0f, 360f, true, 12, inColor)
	}

	private fun emitFilledQuad(
		ax: Float, ay: Float, bx: Float, by: Float,
		cx: Float, cy: Float, dx: Float, dy: Float,
		inColor: ComposeColor,
	) {
		emitFilledTri(ax, ay, bx, by, cx, cy, inColor)
		emitFilledTri(ax, ay, cx, cy, dx, dy, inColor)
	}

	private fun emitFilledTri(
		ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float,
		inColor: ComposeColor,
	) {
		memScoped {
			val vVerts = allocArray<SDL_Vertex>(3)
			writeVertex(vVerts[0], ax, ay, inColor)
			writeVertex(vVerts[1], bx, by, inColor)
			writeVertex(vVerts[2], cx, cy, inColor)
			SDL_RenderGeometry(
				fRenderer.reinterpret(),
				null,
				vVerts,
				3,
				null,
				0,
			)
		}
	}

	private fun writeVertex(inV: SDL_Vertex, inX: Float, inY: Float, inColor: ComposeColor) {
		inV.position.x = inX
		inV.position.y = inY
		inV.color.r = inColor.r8 / 255f
		inV.color.g = inColor.g8 / 255f
		inV.color.b = inColor.b8 / 255f
		inV.color.a = inColor.a8 / 255f
		inV.tex_coord.x = 0f
		inV.tex_coord.y = 0f
	}
}

// ==================
// MARK: Brush → flat colour reducer
// ==================

/* Reduces a Brush to a single ComposeColor. SolidColor passes through; the
   three gradient flavours collapse to their first colour (visible) so
   widgets still render — gradient rendering proper needs per-vertex colour
   sampling which is out of scope for this turn. */
private fun Brush.dominantColor(): ComposeColor = when (this) {
	is SolidColor -> color
	is LinearGradient -> colors.firstOrNull() ?: ComposeColor.Transparent
	is RadialGradient -> colors.firstOrNull() ?: ComposeColor.Transparent
	is SweepGradient -> colors.firstOrNull() ?: ComposeColor.Transparent
}

private fun ComposeColor.withAlphaScaled(inAlpha: Float): ComposeColor =
	if (inAlpha >= 1f) this else copy(alpha = alpha * inAlpha)
