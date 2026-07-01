package androidx.compose.foundation.text

// ==================
// MARK: ContextMenuIcons — native actual (id bag)
// ==================

@kotlin.jvm.JvmInline
internal actual value class ContextMenuIcons actual constructor(actual val value: Int) {
	actual companion object {
		actual val ActionModeCutDrawable: ContextMenuIcons = ContextMenuIcons(0)
		actual val ActionModeCopyDrawable: ContextMenuIcons = ContextMenuIcons(1)
		actual val ActionModePasteDrawable: ContextMenuIcons = ContextMenuIcons(2)
		actual val ActionModeSelectAllDrawable: ContextMenuIcons = ContextMenuIcons(3)
		actual val ID_NULL: ContextMenuIcons = ContextMenuIcons(-1)
	}
}
