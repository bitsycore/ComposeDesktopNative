package com.compose.desktop.native.input

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred

// ==================
// MARK: PointerInputEvent (project-only render-bridge)
// ==================

/* Suspending DSL view of a pointer event: a list of changes (we ship one
   change per event today; multi-touch can extend later). No upstream
   equivalent in androidx.* — upstream's `PointerEvent` has a different
   shape and an engine-tied pointer pipeline. Per FIDELITY relocate rule,
   lives in com.compose.desktop.native.input.

   Distinct from the simpler `androidx.compose.ui.input.pointer.PointerEvent`
   that ComposeWindow uses internally for legacy dispatch (x, y, type, button). */
class PointerInputEvent(val changes: List<PointerInputChange>)

// ==================
// MARK: PointerInputElement (modifier)
// ==================

/* Modifier.Element that the host (ComposeWindow) hands every relevant
   pointer event to. Owns a PointerInputScope whose suspending block is
   driven by a LaunchedEffect tied to the user-supplied key(s).

   .scope is public to give :window the ability to dispatch pointer events
   into it. App code should not poke at the scope directly — interact with
   it via the suspending block you pass to Modifier.pointerInput. */
class PointerInputElement(val scope: PointerInputScopeImpl) : Modifier.Element

// ==================
// MARK: PointerInputScopeImpl
// ==================

/* Concrete PointerInputScope held by every PointerInputElement.
   Exposes deliverChange() for the renderer host to push events in.
   Public for cross-module visibility (Kotlin's `internal` is per-module,
   and :window needs to call deliverChange). */
class PointerInputScopeImpl : PointerInputScope {

	// One in-flight awaiter at a time — pointerInput { } blocks are
	// strictly sequential (matches upstream semantics for a single
	// awaitPointerEventScope coroutine).
	private var fAwaiter: CompletableDeferred<PointerInputEvent>? = null
	private var fLastChange: PointerInputChange? = null

	override suspend fun <R> awaitPointerEventScope(
		block: suspend AwaitPointerEventScope.() -> R,
	): R {
		val vScope = object : AwaitPointerEventScope {
			override suspend fun awaitPointerEvent(): PointerInputEvent {
				val vDeferred = CompletableDeferred<PointerInputEvent>()
				fAwaiter = vDeferred
				try {
					return vDeferred.await()
				} catch (t: CancellationException) {
					if (fAwaiter === vDeferred) fAwaiter = null
					throw t
				}
			}
		}
		return vScope.block()
	}

	/* Deliver a change from the host. Computes pressed-transition fields
	   off the LAST change we delivered. If nothing is awaiting, the
	   event is dropped — same as upstream when no suspension is active.
	   Called by the renderer host (:window) — not API for app code. */
	fun deliverChange(position: Offset, pressed: Boolean, id: Long) {
		val vPrev = fLastChange
		val vChange = PointerInputChange(
			id = id,
			position = position,
			pressed = pressed,
			previousPosition = vPrev?.position ?: position,
			previousPressed = vPrev?.pressed ?: false,
		)
		fLastChange = vChange
		val vA = fAwaiter
		fAwaiter = null
		vA?.complete(PointerInputEvent(listOf(vChange)))
	}
}
