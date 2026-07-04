package androidx.compose.foundation.style

import androidx.compose.foundation.text.modifiers.StylePhase
import androidx.compose.foundation.text.modifiers.TextStyleProviderNode
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

// ==================
// MARK: OuterNodeKey / StyleOuterNode — minimal shim
// ==================
//
// Upstream `foundation.style.` is a 7-file / ~7000L experimental style system
// (StyleScope + StyleProperties + StyleModifier + StyleAnimations + StyleState
// + ResolvedStyle + Style) that we don't vendor. Vendored upstream
// `TextStyleProviderNode.kt` needs just two symbols from that package:
//
//   * `OuterNodeKey` — the string used as `traverseKey` when walking ancestors
//     looking for a style-provider node.
//   * `StyleOuterNode` — the node class that gets `is`-checked inside the
//     `traverseAncestors(OuterNodeKey) { ... }` block.
//
// Since nothing in our tree ever constructs a `StyleOuterNode` (no
// `Modifier.style { }` chain exists here — it's part of the experimental
// system), the traversal block is never entered and `inheritedTextStyle`
// short-circuits back to `fallback`. This shim just gives the compiler the
// symbols it needs.
//
// TODO: delete this file if the full `foundation.style.` package ever vendors.

internal const val OuterNodeKey: String = "StyleOuterNode"

internal class StyleOuterNode : Modifier.Node(), TextStyleProviderNode {
	override val traverseKey: Any get() = OuterNodeKey
	override fun computeInheritedTextStyle(phase: StylePhase, fallback: TextStyle): TextStyle = fallback
}
