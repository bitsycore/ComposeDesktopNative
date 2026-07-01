package androidx.compose.ui.draganddrop

// ==================
// MARK: DragAndDropManager shim
// ==================

/**
 * Marker for upstream `androidx.compose.ui.draganddrop.DragAndDropManager`.
 * Real interface owns modifiers registration + drag session lifecycle;
 * marker is enough for vendored Owner.
 */
interface DragAndDropManager {
	val isRequestDragAndDropTransferRequired: Boolean get() = false
	fun requestDragAndDropTransfer(
		@Suppress("UNUSED_PARAMETER") node: Any,
		@Suppress("UNUSED_PARAMETER") offset: androidx.compose.ui.geometry.Offset,
	) {}
	fun registerNodeInterest(@Suppress("UNUSED_PARAMETER") node: Any) {}
	fun isInterestedNode(@Suppress("UNUSED_PARAMETER") node: Any): Boolean = false
}
