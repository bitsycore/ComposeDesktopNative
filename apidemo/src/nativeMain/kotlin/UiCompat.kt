package apidemo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.compose.desktop.native.layout.intOffset
import com.compose.desktop.native.text.currentViewportHeight
import com.compose.desktop.native.text.currentViewportWidth
import com.compose.desktop.native.window.PopupOutsideDismiss
import com.compose.desktop.native.window.PositionedPopup
import kotlinx.coroutines.delay

// ==================
// MARK: UiCompat — project-local widgets that used to come from :material
// ==================

/* When :apidemo was migrated to :material3, these four widgets had no
   drop-in equivalent in Material 3:

   - `Dialog(onDismissRequest, content)` — m3 has `AlertDialog` + `BasicAlertDialog`
      but no plain scrim-Dialog wrapper; the app wants a fullscreen scrim + centred
      content Surface. Ported from the retired :material Dialog verbatim.
   - `DropdownMenu(expanded, onDismissRequest, anchor, offsetX, offsetY, minWidth, content)`
      — m3's DropdownMenu takes a `DpOffset` and no anchor state; positioning is
      internal to its own Popup wrapper. The project's anchor-based API is better
      for our layout pattern (trigger widget writes into a `MenuAnchorState`).
   - `DropdownMenuItem(onClick, modifier, enabled, content)` — m3 requires explicit
      `text` / `leadingIcon` / `trailingIcon` slots. Ours is content-based and calls
      out to a MenuRow helper for icon-text rows.
   - `TooltipBox(text, delayMillis, content)` — m3's TooltipBox is state-based with
      a `PopupPositionProvider` + `TooltipScope`. We just want a hover-delay tooltip.

   All four live in `apidemo` package (no import beyond the file) so the retirement
   of :material doesn't force call-site rewrites. `MaterialTheme.colors.surface` /
   `.onSurface` swapped to m3's `colorScheme.surface` / `.onSurface`. */

// ==================
// MARK: Dialog
// ==================

@Composable
fun Dialog(
	onDismissRequest: () -> Unit,
	content: @Composable () -> Unit,
) {
	// Neither the scrim nor the content-swallower should paint any hover / press
	// indication (they exist only to route clicks). LocalIndication resolves to
	// DefaultDebugIndication here, which paints a 10% black overlay on hover —
	// on the fullscreen scrim that made the whole dialog visibly darker when
	// the mouse entered; on the content-swallower it darkened the dialog body.
	// Explicit interactionSource + indication=null suppresses the overlay
	// without affecting click routing.
	val vScrimInteraction = remember { MutableInteractionSource() }
	val vContentInteraction = remember { MutableInteractionSource() }
	Popup(onDismissRequest = onDismissRequest) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(Color(0x80000000L))
				.clickable(
					interactionSource = vScrimInteraction,
					indication = null,
					onClick = onDismissRequest,
				),
			contentAlignment = Alignment.Center,
		) {
			// Stop scrim click-through: any click inside the dialog body itself
			// should be a no-op (it landed inside the content, not on the scrim).
			Box(
				modifier = Modifier
					.widthIn(min = DialogDefaults.MinWidth, max = DialogDefaults.MaxWidth)
					.background(MaterialTheme.colorScheme.surface, DialogDefaults.Shape)
					.clickable(
						interactionSource = vContentInteraction,
						indication = null,
						onClick = { /* swallow */ },
					),
			) { content() }
		}
	}
}

object DialogDefaults {
	val MinWidth: Dp = 280.dp
	val MaxWidth: Dp = 560.dp
	val Shape = RoundedCornerShape(8.dp)
}

// ==================
// MARK: DropdownMenu
// ==================

/* Anchored popup menu. Two ways to position it:
   - `anchor` (recommended): a state object the trigger widget updates via
     Modifier.menuAnchor(...). Menu lands just below the trigger's bottom-left.
   - `offsetX` / `offsetY`: absolute window coordinates for context menus. */
@Composable
fun DropdownMenu(
	expanded: Boolean,
	onDismissRequest: () -> Unit,
	anchor: MenuAnchorState? = null,
	offsetX: Dp = 0.dp,
	offsetY: Dp = 0.dp,
	minWidth: Dp = DropdownMenuDefaults.MinWidth,
	content: @Composable () -> Unit,
) {
	if (!expanded) return
	val vDensity = LocalDensity.current
	val vOffsetXPx = with(vDensity) { offsetX.toPx().toInt() }
	val vOffsetYPx = with(vDensity) { offsetY.toPx().toInt() }
	val vAnchorTop = (anchor?.position?.y ?: 0) + vOffsetYPx
	val vAnchorBottom = vAnchorTop + (anchor?.size?.height ?: 0)
	val vBaseX = (anchor?.position?.x ?: 0) + vOffsetXPx

	Popup(onDismissRequest = onDismissRequest) {
		val vWinW = currentViewportWidth
		val vWinH = currentViewportHeight
		var vMenu by remember { mutableStateOf(IntSize.Zero) }
		val vBelowY = vAnchorBottom
		val vAboveY = vAnchorTop - vMenu.height
		// Flip menu above anchor when it would overflow the bottom, clamp X to viewport.
		val vY = if (vMenu.height > 0 && vWinH > 0 && vBelowY + vMenu.height > vWinH && vAboveY >= 0) vAboveY else vBelowY
		val vX = if (vMenu.width > 0 && vWinW > 0) vBaseX.coerceIn(0, (vWinW - vMenu.width).coerceAtLeast(0)) else vBaseX
		// Swallower: hovering the popup shouldn't paint DefaultDebugIndication's 10%
		// black overlay (per-item hover feedback keeps the default indication).
		val vSwallowInteraction = remember { MutableInteractionSource() }
		Box(
			modifier = Modifier
				.offset { IntOffset(vX, vY) }
				.onSizeChanged { vMenu = it }
				.width(minWidth)
				.background(MaterialTheme.colorScheme.surface, DropdownMenuDefaults.Shape)
				.border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f), DropdownMenuDefaults.Shape)
				.padding(vertical = 4.dp)
				.clickable(
					interactionSource = vSwallowInteraction,
					indication = null,
					onClick = { /* swallow */ },
				)
		) {
			Column(modifier = Modifier.fillMaxWidth()) { content() }
		}
		PopupOutsideDismiss(vX, vY, vMenu.width, vMenu.height, onDismissRequest)
	}
}

@Composable
fun DropdownMenuItem(
	onClick: () -> Unit,
	modifier: Modifier = Modifier,
	enabled: Boolean = true,
	content: @Composable () -> Unit,
) {
	val vHoverSrc = remember { MutableInteractionSource() }
	val vHover by vHoverSrc.collectIsHoveredAsState()
	val vBg = when {
		!enabled -> Color.Transparent
		vHover   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
		else     -> Color.Transparent
	}
	Box(
		modifier = modifier
			.fillMaxWidth()
			.defaultMinSize(minHeight = DropdownMenuDefaults.ItemHeight)
			.background(vBg)
			.hoverable(vHoverSrc)
			.clickable { if (enabled) onClick() }
			.padding(horizontal = 16.dp),
		contentAlignment = Alignment.CenterStart,
	) {
		Row(verticalAlignment = Alignment.CenterVertically) { content() }
	}
}

object DropdownMenuDefaults {
	val MinWidth: Dp = 180.dp
	val ItemHeight: Dp = 36.dp
	val Shape = RoundedCornerShape(4.dp)
}

// ==================
// MARK: MenuAnchorState
// ==================

class MenuAnchorState {
	var position: IntOffset by mutableStateOf(IntOffset.Zero)
		internal set
	var size: IntSize by mutableStateOf(IntSize.Zero)
		internal set
}

@Composable
fun rememberMenuAnchor(): MenuAnchorState = remember { MenuAnchorState() }

fun Modifier.menuAnchor(inAnchor: MenuAnchorState): Modifier = this
	.onGloballyPositioned { inAnchor.position = it.intOffset }
	.onSizeChanged { inAnchor.size = it }

// ==================
// MARK: TooltipBox
// ==================

@Composable
fun TooltipBox(
	text: String,
	modifier: Modifier = Modifier,
	delayMillis: Long = TooltipDefaults.DelayMillis,
	content: @Composable () -> Unit,
) {
	val vHoverSrc = remember { MutableInteractionSource() }
	val vHover by vHoverSrc.collectIsHoveredAsState()
	var vVisible by remember { mutableStateOf(false) }
	var vPos by remember { mutableStateOf(IntOffset.Zero) }
	var vHeight by remember { mutableStateOf(0) }

	LaunchedEffect(vHover) {
		if (vHover) {
			delay(delayMillis)
			if (vHover) vVisible = true
		} else {
			vVisible = false
		}
	}

	Box(
		modifier = modifier
			.hoverable(vHoverSrc)
			.onGloballyPositioned { vPos = it.intOffset }
			.onSizeChanged { vHeight = it.height }
	) {
		content()
		if (vVisible) {
			val vGapPx = with(LocalDensity.current) { TooltipDefaults.GapDp.toPx().toInt() }
			PositionedPopup(
				x = vPos.x,
				y = vPos.y + vHeight + vGapPx,
				onDismissRequest = { vVisible = false },
			) {
				Box(
					modifier = Modifier
						.background(TooltipDefaults.BackgroundColor, TooltipDefaults.Shape)
						.padding(horizontal = 8.dp, vertical = 4.dp)
				) {
					Text(
						text = text,
						color = TooltipDefaults.ContentColor,
						fontSize = 12.sp,
					)
				}
			}
		}
	}
}

object TooltipDefaults {
	val DelayMillis: Long = 600L
	val BackgroundColor: Color = Color(0xE6111111L)
	val ContentColor: Color = Color.White
	val Shape = RoundedCornerShape(4.dp)
	val GapDp: Dp = 4.dp
}
