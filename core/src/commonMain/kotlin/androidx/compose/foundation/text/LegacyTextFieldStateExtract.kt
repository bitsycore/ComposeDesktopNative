package androidx.compose.foundation.text

import androidx.compose.runtime.RecomposeScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.EditProcessor
import androidx.compose.ui.text.input.TextInputSession
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==================
// MARK: LegacyTextFieldState — extract
// ==================

/*
 Extracted from upstream `foundation.text.CoreTextField.kt` (1200+L). We're
 not vendoring the whole CoreTextField file yet (needs 30+ other unvendored
 symbols — CoreTextFieldSemanticsModifier, TextFieldSelectionHandle,
 LocalFocusManager, LocalSoftwareKeyboardController, LocalWindowInfo,
 createLegacyPlatformTextInputServiceAdapter, etc). But TextFieldSelectionManager
 references this class heavily as its state anchor — extract it here so the
 selection manager can vendor.

 TODO: delete this file once CoreTextField.kt can vendor cleanly.
*/

/** Small marker + property (matches upstream `HeightForSingleLineFieldProvider`
 *  in `foundation.text.input.internal.TextLayoutState.kt` — moved here so we
 *  don't need to vendor TextLayoutState just for this interface). */
internal interface HeightForSingleLineFieldProvider {
	var heightForSingleLineField: Dp
}

internal class LegacyTextFieldState(
	var textDelegate: TextDelegate,
	val recomposeScope: RecomposeScope,
	val keyboardController: SoftwareKeyboardController?,
) : HeightForSingleLineFieldProvider {
	val processor = EditProcessor()
	var inputSession: TextInputSession? = null

	var hasFocus by mutableStateOf(false)

	override var heightForSingleLineField by mutableStateOf(0.dp)

	private var _layoutCoordinates: LayoutCoordinates? = null
	var layoutCoordinates: LayoutCoordinates?
		get() = _layoutCoordinates?.takeIf { it.isAttached }
		set(value) { _layoutCoordinates = value }

	private val layoutResultState = mutableStateOf<TextLayoutResultProxy?>(null)
	var layoutResult: TextLayoutResultProxy?
		get() = layoutResultState.value
		set(value) {
			layoutResultState.value = value
			isLayoutResultStale = false
		}

	var untransformedText: AnnotatedString? = null

	var handleState by mutableStateOf(HandleState.None)
	var showFloatingToolbar by mutableStateOf(false)
	var showSelectionHandleStart by mutableStateOf(false)
	var showSelectionHandleEnd by mutableStateOf(false)
	var showCursorHandle by mutableStateOf(false)

	var isLayoutResultStale: Boolean = true
		private set

	var isInTouchMode: Boolean by mutableStateOf(false)

	var deletionPreviewHighlightRange: TextRange by mutableStateOf(TextRange.Zero)
	var selectionPreviewHighlightRange: TextRange by mutableStateOf(TextRange.Zero)

	/** Marks the layout result stale — the layout will be recomputed on next measure. */
	fun invalidateLayoutResult() {
		isLayoutResultStale = true
	}
}
