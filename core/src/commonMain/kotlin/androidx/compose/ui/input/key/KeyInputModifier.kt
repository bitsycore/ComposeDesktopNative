package androidx.compose.ui.input.key

import com.compose.desktop.native.element.KeyEventDispatch
import androidx.compose.ui.Modifier
import com.compose.desktop.native.element.OnKeyEventModifier

// ==================
// MARK: Modifier.onKeyEvent
// ==================

/* Receives raw key events while the node (or a focusable descendant) is
   focused. Return true to consume the event; false bubbles to the next
   handler up the focus chain. NOTE: official onKeyEvent takes (KeyEvent) ->
   Boolean; this passes the project's KeyEventDispatch wrapper (see CLAUDE.md
   known-diverging). */
fun Modifier.onKeyEvent(onKeyEvent: (KeyEventDispatch) -> Boolean) =
	then(OnKeyEventModifier(onKeyEvent))
