package androidx.compose.foundation.text.input.internal

import androidx.compose.foundation.content.internal.ReceiveContentConfiguration
import androidx.compose.ui.platform.PlatformTextInputSession
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableSharedFlow

// ==================
// MARK: TextFieldDecoratorModifier — platformSpecificTextInputSession native actual
// ==================

/*
 No IME wiring yet on desktop — this actual just hangs (awaitCancellation)
 so the caller's `establishTextInputSession` block runs forever, letting
 the vendored decorator drive the field via key events instead.
 TODO: wire real IME through SDL3_ttf when the platform text-input session
 lands.
*/
internal actual suspend fun PlatformTextInputSession.platformSpecificTextInputSession(
	state: TransformedTextFieldState,
	layoutState: TextLayoutState,
	imeOptions: ImeOptions,
	receiveContentConfiguration: ReceiveContentConfiguration?,
	onImeAction: ((ImeAction) -> Unit)?,
	updateSelectionState: (() -> Unit)?,
	stylusHandwritingTrigger: MutableSharedFlow<Unit>?,
	viewConfiguration: ViewConfiguration?,
	updateTouchMode: (Boolean) -> Unit,
): Nothing = awaitCancellation()
