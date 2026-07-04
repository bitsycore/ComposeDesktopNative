package androidx.compose.foundation.text

import androidx.compose.foundation.contextmenu.ContextMenuScope
import androidx.compose.foundation.contextmenu.ContextMenuState
import androidx.compose.foundation.contextmenu.close
import androidx.compose.foundation.text.contextmenu.data.TextContextMenuKeys
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import kotlin.jvm.JvmInline

// ==================
// MARK: TextContextMenuItems / TextItem / MenuItemsAvailability — extract
// ==================

/*
 Extracted verbatim from upstream `foundation.text.CommonContextMenuArea.kt`
 (only the enum + helper + value class — the surrounding `CommonContextMenuArea`
 Composables need SelectionManager / TextFieldSelectionManager /
 TextFieldSelectionState / ProvideDefaultPlatformTextContextMenuProviders,
 all unvendored). SelectionManager references TextContextMenuItems +
 TextItem + MenuItemsAvailability, so this project extract unblocks it.

 TODO: delete this file when CommonContextMenuArea.kt can vendor cleanly
 (needs the TextField ecosystem + ProvideDefaultPlatformTextContextMenuProviders
 platform actual).
*/

/** The default text context menu items. */
internal enum class TextContextMenuItems(
	val key: Any,
	val stringId: ContextMenuStrings,
	val drawableId: ContextMenuIcons,
) {
	Cut(
		key = TextContextMenuKeys.CutKey,
		stringId = ContextMenuStrings.Cut,
		drawableId = ContextMenuIcons.ActionModeCutDrawable,
	),
	Copy(
		key = TextContextMenuKeys.CopyKey,
		stringId = ContextMenuStrings.Copy,
		drawableId = ContextMenuIcons.ActionModeCopyDrawable,
	),
	Paste(
		key = TextContextMenuKeys.PasteKey,
		stringId = ContextMenuStrings.Paste,
		drawableId = ContextMenuIcons.ActionModePasteDrawable,
	),
	SelectAll(
		key = TextContextMenuKeys.SelectAllKey,
		stringId = ContextMenuStrings.SelectAll,
		drawableId = ContextMenuIcons.ActionModeSelectAllDrawable,
	),
	Autofill(
		key = TextContextMenuKeys.AutofillKey,
		stringId = ContextMenuStrings.Autofill,
		drawableId = ContextMenuIcons.ID_NULL,
	);

	@ReadOnlyComposable @Composable fun resolvedString(): String = getString(stringId)
}

internal inline fun ContextMenuScope.TextItem(
	state: ContextMenuState,
	label: TextContextMenuItems,
	enabled: Boolean,
	crossinline operation: () -> Unit,
) {
	// b/365619447 - instead of setting `enabled = enabled` in `item`,
	//  just remove the item from the menu.
	if (enabled) {
		item(label = { label.resolvedString() }) {
			operation()
			state.close()
		}
	}
}

@JvmInline
internal value class MenuItemsAvailability private constructor(val value: Int) {
	constructor(
		canCopy: Boolean,
		canPaste: Boolean,
		canCut: Boolean,
		canSelectAll: Boolean,
		canAutofill: Boolean,
	) : this(
		(if (canCopy) COPY else 0) or
			(if (canPaste) PASTE else 0) or
			(if (canCut) CUT else 0) or
			(if (canSelectAll) SELECT_ALL else 0) or
			(if (canAutofill) AUTO_FILL else 0)
	)

	companion object {
		private const val COPY = 0b0001
		private const val PASTE = 0b0010
		private const val CUT = 0b0100
		private const val SELECT_ALL = 0b1000
		private const val AUTO_FILL = 0b10000
		private const val NONE = 0

		val None = MenuItemsAvailability(NONE)
	}

	val canCopy
		get() = value and COPY == COPY

	val canPaste
		get() = value and PASTE == PASTE

	val canCut
		get() = value and CUT == CUT

	val canSelectAll
		get() = value and SELECT_ALL == SELECT_ALL

	val canAutofill
		get() = value and AUTO_FILL == AUTO_FILL
}
