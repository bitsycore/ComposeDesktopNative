package androidx.compose.foundation.layout

import androidx.compose.ui.unit.Dp
import kotlin.math.max

// ==================
// MARK: Arrangement
// ==================

object Arrangement {

    fun interface Horizontal {
        fun arrange(totalSize: Int, sizes: List<Int>, outPositions: IntArray)
    }

    fun interface Vertical {
        fun arrange(totalSize: Int, sizes: List<Int>, outPositions: IntArray)
    }

    /** Can be used as both Horizontal and Vertical. */
    interface HorizontalOrVertical : Horizontal, Vertical

    // ============
    //  Horizontal-only

    val Start = Horizontal { _, sizes, out ->
        var current = 0
        sizes.forEachIndexed { i, size -> out[i] = current; current += size }
    }

    val End = Horizontal { totalSize, sizes, out ->
        var current = totalSize - sizes.sum()
        sizes.forEachIndexed { i, size -> out[i] = current; current += size }
    }

    // ============
    //  Vertical-only

    val Top = Vertical { _, sizes, out ->
        var current = 0
        sizes.forEachIndexed { i, size -> out[i] = current; current += size }
    }

    val Bottom = Vertical { totalSize, sizes, out ->
        var current = totalSize - sizes.sum()
        sizes.forEachIndexed { i, size -> out[i] = current; current += size }
    }

    // ============
    //  HorizontalOrVertical

    val Center = object : HorizontalOrVertical {
        override fun arrange(totalSize: Int, sizes: List<Int>, outPositions: IntArray) {
            var current = (totalSize - sizes.sum()) / 2
            sizes.forEachIndexed { i, size -> outPositions[i] = current; current += size }
        }
    }

    val SpaceBetween = object : HorizontalOrVertical {
        override fun arrange(totalSize: Int, sizes: List<Int>, outPositions: IntArray) {
            if (sizes.size <= 1) {
                sizes.forEachIndexed { i, _ -> outPositions[i] = 0 }
                return
            }
            val gap = (totalSize - sizes.sum()) / (sizes.size - 1)
            var current = 0
            sizes.forEachIndexed { i, size -> outPositions[i] = current; current += size + gap }
        }
    }

    val SpaceEvenly = object : HorizontalOrVertical {
        override fun arrange(totalSize: Int, sizes: List<Int>, outPositions: IntArray) {
            val gap = (totalSize - sizes.sum()) / (sizes.size + 1)
            var current = gap
            sizes.forEachIndexed { i, size -> outPositions[i] = current; current += size + gap }
        }
    }

    val SpaceAround = object : HorizontalOrVertical {
        override fun arrange(totalSize: Int, sizes: List<Int>, outPositions: IntArray) {
            val gap = (totalSize - sizes.sum()) / max(1, sizes.size)
            var current = gap / 2
            sizes.forEachIndexed { i, size -> outPositions[i] = current; current += size + gap }
        }
    }

    fun spacedBy(space: Dp): HorizontalOrVertical = object : HorizontalOrVertical {
        override fun arrange(totalSize: Int, sizes: List<Int>, outPositions: IntArray) {
            val gap = space.value.toInt()
            var current = 0
            sizes.forEachIndexed { i, size -> outPositions[i] = current; current += size + gap }
        }
    }
}
