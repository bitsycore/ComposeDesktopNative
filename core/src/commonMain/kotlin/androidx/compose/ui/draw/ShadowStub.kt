package androidx.compose.ui.draw

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ==================
// MARK: Modifier.shadow — project stub
// ==================

/*
 Upstream `Modifier.shadow(...)` (ui.draw.Shadow.kt, 894L) lowers to a
 `ShadowGraphicsLayerElement` that draws an elevation-based shadow via
 `DropShadowPainter` / `InnerShadowPainter` / `PlatformShadowContext`. None
 of those are vendored yet (blocked on Skia blur + `ShadowContext.shim`),
 so this project stub swallows the parameters and returns the receiver
 unchanged. `Modifier.shadow(...)` compiles and cascades through the tree
 with no runtime shadow.

 TODO: real drop-shadow rendering — wire ShadowContext into both renderer
 backends (Skia already has `Skia.BlurMaskFilter`; SDL3 has none, so it
 needs a software-blurred pre-render or accepts a "no shadow" gap).

 Delete this file once upstream `ui.draw.Shadow.kt` can vendor cleanly.
*/
@Stable
fun Modifier.shadow(
	elevation: Dp,
	shape: Shape = RectangleShape,
	clip: Boolean = elevation > 0.dp,
	ambientColor: Color = DefaultShadowColor,
	spotColor: Color = DefaultShadowColor,
): Modifier = this
