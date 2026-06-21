package androidx.compose.animation.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.CancellationException

// ==================
// MARK: Animatable
// ==================

/* Imperative animation handle. Holds a state-backed `value` and lets
   callers `snapTo(target)` (instant) or `animateTo(target, spec)`
   (suspend until the spec finishes). Concurrent animateTo calls cancel
   the previous one, like the upstream API. */
class Animatable<T>(
	inInitial: T,
	private val fLerp: (T, T, Float) -> T,
) {
	private var fValue by mutableStateOf(inInitial)
	private var fJobId: Int = 0  // bumped on every animateTo to cancel prior runs

	val value: T get() = fValue
	var targetValue: T = inInitial
		private set
	var isRunning: Boolean = false
		private set

	/* Jump to the target without animating. Cancels any in-flight
	   animateTo. */
	suspend fun snapTo(inTarget: T) {
		fJobId++
		fValue = inTarget
		targetValue = inTarget
		isRunning = false
	}

	/* Animate from the current value to inTarget using the given spec.
	   Suspends until the animation completes or is cancelled by a
	   subsequent call (or coroutine cancellation). */
	suspend fun animateTo(
		inTarget: T,
		inSpec: AnimationSpec<T> = tween(),
	): AnimationResult<T> {
		fJobId++
		val vMyId = fJobId
		val vFrom = fValue
		targetValue = inTarget
		isRunning = true

		val vStartNanos = withFrameNanos { it }
		try {
			while (true) {
				val vNow = withFrameNanos { it }
				if (fJobId != vMyId) return AnimationResult(fValue, AnimationEndReason.Cancelled)
				val vElapsedMs = ((vNow - vStartNanos) / 1_000_000).toInt()
				val (vV, vDone) = evaluateSpec(inSpec, vFrom, inTarget, vElapsedMs, fLerp)
				fValue = vV
				if (vDone) {
					isRunning = false
					return AnimationResult(vV, AnimationEndReason.Finished)
				}
			}
		} catch (t: CancellationException) {
			isRunning = false
			throw t
		}
	}

	/* Stop without snapping; current `value` stays where it is. */
	fun stop() {
		fJobId++
		isRunning = false
	}
}

enum class AnimationEndReason { Finished, Cancelled }

data class AnimationResult<T>(val endValue: T, val endReason: AnimationEndReason)

// ==================
// MARK: Composable factories
// ==================

@Composable
fun Animatable(inInitial: Float): Animatable<Float> =
	remember { Animatable(inInitial) { vA, vB, vF -> vA + (vB - vA) * vF } }
