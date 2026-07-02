package androidx.compose.ui.draganddrop

import androidx.compose.ui.node.DelegatableNode

// ==================
// MARK: DragAndDropNode — shim
// ==================

/*
 Phase 9 stub — real upstream DragAndDropNode is 492L implementing a full
 drag session lifecycle (accept / drop / hover cascade across the modifier
 tree). Vendored DragAndDropManager references it by name only —
 `fun requestDragAndDropTransfer(node: DragAndDropNode, offset: Offset)`.

 Marker interface is enough until the drag/drop pipeline is wired end-to-end
 through the vendored Owner.
*/
interface DragAndDropNode : DelegatableNode
