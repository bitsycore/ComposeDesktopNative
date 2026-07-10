package apidemo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.compose.sdl.layout.intOffset
import com.compose.sdl.window.PositionedPopup
import kotlinx.coroutines.delay

// ==================
// MARK: UiCompat — project-local widgets that used to come from :material
// ==================

/* Two project-local widgets with no drop-in Material 3 equivalent:

   - `Dialog(onDismissRequest, content)` — m3 has `AlertDialog` + `BasicAlertDialog`
      but no plain scrim-Dialog wrapper; the app wants a fullscreen scrim + centred
      content Surface. Ported from the retired :material Dialog verbatim.
   - `TooltipBox(text, delayMillis, content)` — m3's TooltipBox is state-based with
      a `PopupPositionProvider` + `TooltipScope`. We just want a hover-delay tooltip.

   (The old anchor-based DropdownMenu / DropdownMenuItem were retired — call sites now
   use androidx.compose.material3.DropdownMenu directly. Right-click context menus that
   used to open at the cursor now anchor to their trigger element, since m3's DropdownMenu
   is parent-anchored with only a relative offset.)

   Both live in the `apidemo` package (no import beyond the file). */

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
