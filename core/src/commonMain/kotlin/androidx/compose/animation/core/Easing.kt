package androidx.compose.animation.core

// ==================
// MARK: Easing
// ==================

/* Maps [0..1] linear progress to [0..1] eased progress. Standard upstream
   interface; predefined curves match Material-spec defaults so animations
   "feel right" out of the box. */
fun interface Easing {
	fun transform(inFraction: Float): Float
}

val LinearEasing: Easing = Easing { it }

/* Material standard easing: starts fast, slows into the target. The
   coefficients match upstream `FastOutSlowInEasing`. */
val FastOutSlowInEasing: Easing = CubicBezierEasing(0.4f, 0f, 0.2f, 1f)

/* Outgoing motion (entering / leaving the viewport): accelerate then
   cruise. Matches upstream `FastOutLinearInEasing`. */
val FastOutLinearInEasing: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)

/* Incoming motion (settling into place): cruise then slow. Matches
   upstream `LinearOutSlowInEasing`. */
val LinearOutSlowInEasing: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)

/* Cubic Bézier easing with control points (a, b) and (c, d). Solved by
   bisection on the parameter t given x = fraction (the Bézier is
   parameterised in t, not x, so we have to invert numerically — same
   technique upstream uses). 8 iterations gives sub-pixel error for any
   realistic animation. */
class CubicBezierEasing(
	private val fA: Float,
	private val fB: Float,
	private val fC: Float,
	private val fD: Float,
) : Easing {

	override fun transform(inFraction: Float): Float {
		if (inFraction <= 0f) return 0f
		if (inFraction >= 1f) return 1f
		val vT = solveT(inFraction)
		return bezier(fB, fD, vT)
	}

	// Solve x(t) = inX for t ∈ [0, 1] via bisection.
	private fun solveT(inX: Float): Float {
		var vLo = 0f; var vHi = 1f
		repeat(8) {
			val vMid = (vLo + vHi) * 0.5f
			val vXMid = bezier(fA, fC, vMid)
			if (vXMid < inX) vLo = vMid else vHi = vMid
		}
		return (vLo + vHi) * 0.5f
	}

	// 1D cubic Bézier with implicit P0=0, P3=1 and explicit P1, P2.
	private fun bezier(inP1: Float, inP2: Float, inT: Float): Float {
		val vOne = 1f - inT
		return (3f * vOne * vOne * inT * inP1) +
		       (3f * vOne * inT * inT * inP2) +
		       (inT * inT * inT)
	}
}
