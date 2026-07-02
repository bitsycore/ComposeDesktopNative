package androidx.compose.ui.draganddrop

import androidx.compose.ui.geometry.Offset

// ==================
// MARK: DragAndDrop actuals — native no-op
// ==================

/*
 Actuals for vendored DragAndDrop.kt. Mirrors upstream's macosMain actual —
 both event / transfer-data classes are private-ctor placeholders; the
 positionInRoot extension is a TODO (nothing constructs a DragAndDropEvent
 in our project runtime, so it never fires).
*/

actual class DragAndDropEvent private constructor()

internal actual val DragAndDropEvent.positionInRoot: Offset
	get() = Offset.Zero

actual class DragAndDropTransferData private constructor()
