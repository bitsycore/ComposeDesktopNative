package androidx.compose.ui.layout

import androidx.compose.ui.node.LookaheadDelegate
import androidx.compose.ui.node.NodeCoordinator

// Phase 9 stub — lookahead pipeline unvendored; NodeCoordinator reads `.coordinator`
// and LookaheadDelegate constructs one.
internal class LookaheadLayoutCoordinates(
	val lookaheadDelegate: LookaheadDelegate,
) : LayoutCoordinates {
	val coordinator: NodeCoordinator get() = lookaheadDelegate.coordinator
}
