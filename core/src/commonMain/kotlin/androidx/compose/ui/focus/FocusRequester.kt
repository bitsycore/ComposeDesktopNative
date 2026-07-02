package androidx.compose.ui.focus

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.node.ModifierNodeElement

// ==================
// MARK: FocusRequester
// ==================

/**
 * Handle on a focusable composable, used to call [requestFocus] from anywhere
 * in the composition (e.g. from a button click that should focus a text field).
 *
 * The `Modifier.focusRequester(this)` call binds this requester to its host
 * node; calling [requestFocus] walks back to that node and routes through the
 * active [FocusManager].
 *
 * B6b: this whole surface is dormant until focus is rebuilt on the upstream
 * FocusOwner + FocusTargetNode engine. [requestFocus] / [freeFocus] are no-ops
 * when the installed [FocusManager] is the current window's no-op stub.
 */
class FocusRequester {
	// The upstream BackwardsCompatNode registers itself here as modifier nodes
	// mount / unmount.
	val focusRequesterNodes = mutableListOf<FocusRequesterModifierNode>()

	/** Opaque node handle installed by the focus modifier / focus manager. */
	var attachedNode: Any? = null
	var focusManager: FocusManager? = null

	/** Move focus to the bound node. No-op if no node is bound or no FocusManager is installed. */
	fun requestFocus() {
		val vN = attachedNode ?: return
		focusManager?.focusOnNode(vN)
	}

	/** Clear focus from the bound node. No-op if it isn't the currently focused one. */
	fun freeFocus() {
		focusManager?.clearFocus()
	}
}

// ==================
// MARK: FocusManager
// ==================

/**
 * Active focus controller for a composition. Installed by the renderer host
 * (`:window`) at composeWindow setup so user code can move focus without
 * reaching into the window event loop.
 */
interface FocusManager {

	/** [node] is an opaque handle previously stored on [FocusRequester.attachedNode]. */
	fun focusOnNode(node: Any)
	fun clearFocus()
}

/** CompositionLocal threading the active [FocusManager] through the tree. Reads as null outside composeWindow. */
val LocalFocusManager = compositionLocalOf<FocusManager?> { null }

// ==================
// MARK: FocusRequesterModifier
// ==================

/**
 * `ModifierNodeElement` factory for the project's focusRequester modifier.
 *
 * The window module's bindFocusRequesters() pass walks the tree with
 * `Modifier.foldIn` to discover each `FocusRequesterModifier` (this class
 * IS-A `Modifier.Element` via [ModifierNodeElement]), then pops the host
 * node + the active [FocusManager] onto the requester so `requestFocus()`
 * can resolve them later.
 */
class FocusRequesterModifier(val focusRequester: FocusRequester) :
	ModifierNodeElement<FocusRequesterNode>() {
	override fun create() = FocusRequesterNode(focusRequester)
	override fun update(node: FocusRequesterNode) { node.focusRequester = focusRequester }
	override fun hashCode(): Int = focusRequester.hashCode()
	override fun equals(other: Any?): Boolean =
		other is FocusRequesterModifier && other.focusRequester === focusRequester
}

/** Paired `Modifier.Node` for [FocusRequesterModifier]. Lifecycle dormant until the renderer rewrite drives it. */
class FocusRequesterNode(var focusRequester: FocusRequester) : Modifier.Node()

/** Bind a [FocusRequester] to this node. Pair with `Modifier.focusable` so the node actually accepts focus. */
fun Modifier.focusRequester(focusRequester: FocusRequester): Modifier =
	this.then(FocusRequesterModifier(focusRequester))

// ==================
// MARK: onFocusChanged (standalone)
// ==================

/**
 * `ModifierNodeElement` factory for the project's onFocusChanged modifier.
 *
 * Currently no renderer reader consumes this — `onFocusChanged` on
 * `Modifier.focusable(onFocusChanged = …)` carries the focus callback that
 * the window module actually fires. This standalone element exists for API
 * parity with upstream's `Modifier.onFocusChanged { }` shape and may be
 * wired into the focus pipeline when the renderer rewrite lights up.
 */
class OnFocusChangedModifier(val onChange: (Boolean) -> Unit) :
	ModifierNodeElement<OnFocusChangedNode>() {
	override fun create() = OnFocusChangedNode(onChange)
	override fun update(node: OnFocusChangedNode) { node.onChange = onChange }
	override fun hashCode(): Int = onChange.hashCode()
	override fun equals(other: Any?): Boolean =
		other is OnFocusChangedModifier && other.onChange === onChange
}

class OnFocusChangedNode(var onChange: (Boolean) -> Unit) : Modifier.Node()

fun Modifier.onFocusChanged(onFocusChanged: (Boolean) -> Unit): Modifier =
	this.then(OnFocusChangedModifier(onFocusChanged))
