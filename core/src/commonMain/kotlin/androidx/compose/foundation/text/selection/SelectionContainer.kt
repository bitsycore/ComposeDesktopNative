package androidx.compose.foundation.text.selection

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box

// ==================
// MARK: SelectionContainer
// ==================

/* Makes the text inside selectable with the mouse + copyable with the
   platform Copy shortcut.

   The upstream API is content-agnostic — any descendant Text becomes
   selectable. Our re-implementation supports the *common* case where
   the content is a single Text (or Text(AnnotatedString)) read-only
   block: the text composable checks LocalInSelectionContainer at
   composition time and, when true, routes its render through the
   selectable BasicTextField path (which already supports drag-select
   and clipboard copy).

   Limitation: when wrapped around a styled Text(AnnotatedString), the
   per-run colours are dropped — BasicTextField doesn't carry per-char
   colour spans yet. Selection + copy work; the highlight palette is
   the cost of opting into selectability. Plain Text(String) keeps its
   appearance entirely.

   Selection across multiple sibling Texts isn't tracked here yet
   (each child has its own field, so drag stops at the boundary). */
@Composable
fun SelectionContainer(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
	CompositionLocalProvider(LocalInSelectionContainer provides true) {
		Box(modifier = modifier) { content() }
	}
}

/* True when the composition is inside a SelectionContainer. Text
   composables observe this to decide between their default
   non-selectable render and a selectable BasicTextField fallback. */
val LocalInSelectionContainer = compositionLocalOf { false }

/* Disables selection for any descendant Text — used by widgets like
   chips and tabs that want their own click semantics rather than being
   highjacked by a surrounding SelectionContainer. Matches upstream's
   DisableSelection. */
@Composable
fun DisableSelection(content: @Composable () -> Unit) {
	CompositionLocalProvider(LocalInSelectionContainer provides false) { content() }
}
