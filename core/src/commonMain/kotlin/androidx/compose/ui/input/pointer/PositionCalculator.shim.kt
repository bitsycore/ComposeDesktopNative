package androidx.compose.ui.input.pointer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Matrix

// ==================
// MARK: PositionCalculator / MatrixPositionCalculator shims
// ==================

/**
 * Both live inside upstream `PointerInputEventProcessor.kt` (unvendored —
 * huge pointer-input dispatcher). We split them out here so vendored
 * `Owner : PositionCalculator` works.
 */
internal interface PositionCalculator {
	fun screenToLocal(positionOnScreen: Offset): Offset
	fun localToScreen(localPosition: Offset): Offset
}

internal interface MatrixPositionCalculator : PositionCalculator {
	fun localToScreen(localTransform: Matrix)
}
