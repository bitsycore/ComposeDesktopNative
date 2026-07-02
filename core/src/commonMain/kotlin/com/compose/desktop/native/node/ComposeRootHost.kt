package com.compose.desktop.native.node

import androidx.compose.runtime.Applier
import androidx.compose.ui.node.ComposeOwner
import androidx.compose.ui.node.LayoutNode
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

// ==================
// MARK: ComposeRootHost
// ==================

/*
 Phase 9 B4 — public facade the :window layer drives the upstream layout engine
 through. `LayoutNode` / `Owner` / `ComposeOwner` / `NodeApplier` are all internal
 to :core, so this hides them behind a public surface: [applier] (upcast to
 Applier<*> for the Composition), [attach], [setConstraints], [measureAndLayout].
 The internal [rootNode] is read by the renderer backend (also in :core) to paint.

 Toward the end-state (empty commonMain), the whole window/main-loop moves in here
 as the SDL3 windowing/event actual; for now :window still owns the loop and calls
 this facade.
*/
class ComposeRootHost(inDensity: Float = 1f) {

	internal val rootNode: LayoutNode = LayoutNode().apply {
		// The root measures its children against the incoming constraints and
		// places them at the origin — upstream's RootMeasurePolicy. Without this
		// the root keeps LayoutNode's ErrorMeasurePolicy ("Undefined measure").
		measurePolicy = androidx.compose.ui.layout.RootMeasurePolicy
	}
	private val fOwner = ComposeOwner(rootNode, Density(inDensity), LayoutDirection.Ltr)
	private val fApplier = NodeApplier(rootNode)

	// Upcast to the public supertype so the internal NodeApplier / LayoutNode
	// don't leak across the module boundary; Composition accepts Applier<*>.
	val applier: Applier<*> get() = fApplier

	fun attach() {
		fOwner.attach()
	}

	fun setConstraints(inWidth: Int, inHeight: Int) {
		fOwner.setRootConstraints(Constraints.fixed(inWidth, inHeight))
	}

	fun measureAndLayout() {
		fOwner.measureAndLayout()
	}
}
