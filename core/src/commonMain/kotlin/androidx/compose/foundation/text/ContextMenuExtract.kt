package androidx.compose.foundation.text

import androidx.compose.foundation.contextmenu.ContextMenuArea as UiContextMenuArea
import androidx.compose.foundation.contextmenu.ContextMenuState
import androidx.compose.foundation.contextmenu.close
import androidx.compose.foundation.text.selection.SelectionManager
import androidx.compose.foundation.text.selection.contextMenuBuilder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

// ==================
// MARK: ContextMenu — extract (SelectionManager overload only)
// ==================

/*
 Extracted just the `ContextMenuArea(SelectionManager, ...)` overload — the
 one SelectionContainer needs. Upstream `foundation.text.ContextMenu.kt`
 declares three `expect` overloads (TextFieldSelectionManager /
 TextFieldSelectionState / SelectionManager) that live in per-platform
 files; the first two need the whole TextField ecosystem (LegacyTextFieldState +
 TextFieldSelectionManager) we haven't vendored.

 This project extract just wires the SelectionManager overload to the already-
 vendored `foundation.contextmenu.ContextMenuArea` with the manager's builder.

 TODO: delete when ContextMenu.kt + its per-platform actuals can vendor
 (needs TextFieldSelectionState + TextFieldSelectionManager, both blocked
 on the LegacyTextFieldState + CoreTextField pipeline).
*/
@Composable
internal fun ContextMenuArea(
	manager: SelectionManager,
	content: @Composable () -> Unit,
) {
	val state = remember { ContextMenuState() }
	UiContextMenuArea(
		state = state,
		onDismiss = { state.close() },
		contextMenuBuilderBlock = manager.contextMenuBuilder(state),
		content = content,
	)
}
