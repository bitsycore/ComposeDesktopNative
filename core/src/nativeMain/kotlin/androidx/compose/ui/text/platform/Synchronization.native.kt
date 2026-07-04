package androidx.compose.ui.text.platform

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// Native actual for vendored commonMain `Synchronization.kt` — same pattern
// as the working `androidx.compose.ui.platform.Synchronization.native.kt`
// sibling. Upstream's `Synchronization.skiko.kt` carries
// `@PublishedApi @Suppress("LESS_VISIBLE_TYPE_ACCESS_IN_INLINE_WARNING")` on the
// `synchronized` fn — Kotlin 2.4 promotes that diagnostic to ERROR, so we drop
// the `@PublishedApi` and keep the actual `internal actual inline`. Callers of
// `synchronized` from this repo are all `internal` themselves (vendored text /
// font caches), so no public-inline path exists that would need the
// `@PublishedApi` accessor.
@PublishedApi
internal actual class SynchronizedObject : kotlinx.atomicfu.locks.SynchronizedObject()

@Suppress("NOTHING_TO_INLINE")
internal actual inline fun makeSynchronizedObject(ref: Any?): SynchronizedObject = SynchronizedObject()

@Suppress("BanInlineOptIn")
@OptIn(ExperimentalContracts::class)
internal actual inline fun <R> synchronized(lock: SynchronizedObject, block: () -> R): R {
	contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
	return kotlinx.atomicfu.locks.synchronized(lock, block)
}
