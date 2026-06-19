package androidx.compose.ui

import androidx.compose.ui.graphics.Color

// ==================
// MARK: Modifier
// ==================

interface Modifier {

    fun <R> foldIn(initial: R, operation: (R, Element) -> R): R
    fun <R> foldOut(initial: R, operation: (Element, R) -> R): R
    fun then(other: Modifier): Modifier =
        if (other === Modifier) this else CombinedModifier(this, other)

    interface Element : Modifier {
        override fun <R> foldIn(initial: R, operation: (R, Element) -> R) = operation(initial, this)
        override fun <R> foldOut(initial: R, operation: (Element, R) -> R) = operation(this, initial)
    }

    companion object : Modifier {
        override fun <R> foldIn(initial: R, operation: (R, Element) -> R) = initial
        override fun <R> foldOut(initial: R, operation: (Element, R) -> R) = initial
        override fun then(other: Modifier) = other
        override fun toString() = "Modifier"
    }
}

// ==================
// MARK: CombinedModifier
// ==================

private class CombinedModifier(val outer: Modifier, val inner: Modifier) : Modifier {
    override fun <R> foldIn(initial: R, operation: (R, Modifier.Element) -> R): R =
        inner.foldIn(outer.foldIn(initial, operation), operation)

    override fun <R> foldOut(initial: R, operation: (Modifier.Element, R) -> R): R =
        outer.foldOut(inner.foldOut(initial, operation), operation)

    override fun toString() = "[${outer}, ${inner}]"
}

// ==================
// MARK: Modifier Elements (internal)
// ==================

data class PaddingModifier(
    val start: Int = 0,
    val top: Int = 0,
    val end: Int = 0,
    val bottom: Int = 0
) : Modifier.Element

data class BackgroundModifier(val color: Color) : Modifier.Element
data class BorderModifier(val width: Int, val color: Color) : Modifier.Element

data class SizeModifier(
    val width: Int = -1,
    val height: Int = -1,
    val minWidth: Int = -1,
    val minHeight: Int = -1,
    val maxWidth: Int = -1,
    val maxHeight: Int = -1,
    val fillMaxWidth: Boolean = false,
    val fillMaxHeight: Boolean = false
) : Modifier.Element

data class ClickableModifier(val onClick: () -> Unit) : Modifier.Element
