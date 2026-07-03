package com.compose.desktop.native.node

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerInputEvent
import androidx.compose.ui.input.pointer.PointerInputEventData
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.node.ComposeOwner

// ==================
// MARK: feedPointerToProcessor — native actual
// ==================

/* Builds the internal PointerInputEvent (its constructor is native-only) from a single
   mouse pointer and drives the owner's PointerInputEventProcessor. inType: 0=Move 1=Press 2=Release. */
internal actual fun feedPointerToProcessor(
	inOwner: ComposeOwner,
	inType: Int,
	inUptime: Long,
	inX: Float,
	inY: Float,
) {
	val vPos = Offset(inX, inY)
	val vData = PointerInputEventData(
		id = PointerId(0L),
		uptime = inUptime,
		positionOnScreen = vPos,
		position = vPos,
		down = inType == 1,
		pressure = 1f,
		type = PointerType.Mouse,
		activeHover = inType == 0,
		scaleGestureFactor = 1f,
		panGestureOffset = Offset.Zero,
	)
	val vType = when (inType) {
		1 -> PointerEventType.Press
		2 -> PointerEventType.Release
		else -> PointerEventType.Move
	}
	inOwner.processPointerInput(PointerInputEvent(vType, inUptime, listOf(vData)))
}
