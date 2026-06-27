package androidx.compose.ui.window

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.currentCompositionLocalContext
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.compose.desktop.native.window.LocalPopupHost

// ==================
// MARK: PopupProperties
// ==================

/* Behaviour flags for a Popup. This renderer hosts overlay content without a
   focus system, so focusable / dismissOnBackPress / dismissOnClickOutside /
   clippingEnabled are accepted for source-compatibility with official Compose
   but not all are acted on — callers handle outside-click dismissal via their
   own click-catcher (see DropdownMenu) and Dialog draws its own scrim. */
class PopupProperties(
	val focusable: Boolean = false,
	val dismissOnBackPress: Boolean = true,
	val dismissOnClickOutside: Boolean = true,
	val clippingEnabled: Boolean = true,
	val usePlatformDefaultWidth: Boolean = true,
) {
	override fun equals(other: Any?): Boolean =
		other is PopupProperties &&
			focusable == other.focusable &&
			dismissOnBackPress == other.dismissOnBackPress &&
			dismissOnClickOutside == other.dismissOnClickOutside &&
			clippingEnabled == other.clippingEnabled &&
			usePlatformDefaultWidth == other.usePlatformDefaultWidth

	override fun hashCode(): Int {
		var result = focusable.hashCode()
		result = 31 * result + dismissOnBackPress.hashCode()
		result = 31 * result + dismissOnClickOutside.hashCode()
		result = 31 * result + clippingEnabled.hashCode()
		result = 31 * result + usePlatformDefaultWidth.hashCode()
		return result
	}
}

// ==================
// MARK: Popup
// ==================

/* Overlay primitive matching official Compose's signature. Renders `content`
   at the root of the window (above the main tree), positioned by `alignment`
   within the window and shifted by `offset` (logical points). Outside-click
   dismissal and modality are the caller's responsibility (Dialog draws a
   scrim; DropdownMenu installs its own click-catcher) — see PopupProperties.
   The overlay is hosted by the project's PopupHostState (see
   com.compose.desktop.native.window). */
@Composable
fun Popup(
	alignment: Alignment = Alignment.TopStart,
	offset: IntOffset = IntOffset(0, 0),
	onDismissRequest: (() -> Unit)? = null,
	properties: PopupProperties = PopupProperties(),
	content: @Composable () -> Unit,
) {
	val vHost = LocalPopupHost.current
	val vId = remember { Any() }
	// Snapshot the CompositionLocals in scope at the call site. PopupLayer renders
	// the hosted content at the composition root, so without re-providing these it
	// would only see the root defaults — MaterialTheme and app-level locals set
	// further down the tree would never reach the popup.
	val vLocals = currentCompositionLocalContext
	// Default (TopStart / no offset) renders content verbatim — callers that
	// position themselves (DropdownMenu, Snackbar) are unaffected. Otherwise wrap
	// in a fullscreen aligner + offset.
	val vPositioned: @Composable () -> Unit =
		if (alignment == Alignment.TopStart && offset.x == 0 && offset.y == 0) {
			content
		} else {
			{
				Box(modifier = Modifier.fillMaxSize(), contentAlignment = alignment) {
					Box(modifier = Modifier.offset(offset.x.dp, offset.y.dp)) { content() }
				}
			}
		}
	SideEffect {
		vHost.upsert(vId) {
			CompositionLocalProvider(vLocals) { vPositioned() }
		}
	}
	DisposableEffect(Unit) {
		onDispose { vHost.remove(vId) }
	}
}
