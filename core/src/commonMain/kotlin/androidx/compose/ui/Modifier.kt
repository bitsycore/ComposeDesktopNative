package androidx.compose.ui

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
