package androidx.compose.ui.input.pointer

import androidx.compose.ui.geometry.Offset
import com.compose.desktop.native.input.PointerInputEvent

// ==================
// MARK: PointerInputChange
// ==================

/* One pointer's per-frame snapshot — position now and previously, plus
   pressed bits so handlers can detect transitions (press, release,
   move-while-down). `id` is the pointer index (mouse = 0; touch
   contacts later may carry distinct ids). `consume()` marks the change
   handled so ancestor pointerInput blocks ignore it on subsequent
   passes.

   This is the upstream-named public type; the reduced shape (Long id,
   plain Offset) is a project simplification — FIDELITY flags the full
   value-class redesign as runtime-critical, deferred to its own pass. */
class PointerInputChange(
	val id: Long,
	val position: Offset,
	val pressed: Boolean,
	val previousPosition: Offset,
	val previousPressed: Boolean,
) {
	var consumed: Boolean = false; private set
	fun consume() { consumed = true }
}

// ==================
// MARK: Scopes
// ==================

/* Scope passed to the suspending block of Modifier.pointerInput. Inside
   you typically open an `awaitPointerEventScope { ... }` and loop on
   `awaitPointerEvent()`. */
interface PointerInputScope {

	suspend fun <R> awaitPointerEventScope(
		block: suspend AwaitPointerEventScope.() -> R,
	): R
}

/* The scope inside awaitPointerEventScope where you can suspend on
   pointer events.

   Note: returns `com.compose.desktop.native.input.PointerInputEvent` —
   the project-only render-bridge event type (upstream's same-named
   class is `internal expect`, different shape). */
interface AwaitPointerEventScope {

	suspend fun awaitPointerEvent(): PointerInputEvent
}

// PointerInputElement / PointerInputScopeImpl / PointerInputEvent (the
// render-bridge implementations) live in com.compose.desktop.native.input
// per FIDELITY relocate rule — no official upstream equivalent.
