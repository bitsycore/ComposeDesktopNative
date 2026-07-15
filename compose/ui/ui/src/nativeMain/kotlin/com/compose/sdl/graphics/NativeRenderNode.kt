package com.compose.sdl.graphics

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

// ==================
// MARK: NativeRenderNode — the renderer-agnostic retained display-list node
// ==================

/**
 * The one genuinely renderer-specific piece of the retained-layer engine: a node
 * that RECORDS a draw block once into a retained display list and REPLAYS it every
 * frame, with transform / clip / shadow / layer-paint applied at replay WITHOUT
 * re-recording. This is the seam that lets `GraphicsLayer` + `GraphicsLayerOwnerLayer`
 * be common code across both renderers (see RENDERER_REFACTOR.md §4).
 *
 * Modelled after `org.jetbrains.skiko.node.RenderNode` / upstream
 * `SkiaGraphicsLayer.skiko.kt` so the Skia actual can wrap skiko's `RenderNode`
 * (or `org.jetbrains.skia.Picture`) almost verbatim, and the SDL actual
 * (`SdlRenderNode`) can implement the same contract with a command-list /
 * offscreen-texture display list.
 *
 * Lifecycle: [beginRecording] → draw into the returned [Canvas] → [endRecording];
 * then [drawInto] any number of times; [close] releases native resources.
 *
 * NOTE (Phase 0): this surface is provisional — [layerPaint] and the shadow-colour
 * representation may be refined when the Skia actual is vendored (Phase 1) and the
 * SDL actual is built (Phase 2/4).
 */
internal interface NativeRenderNode {

	// ============
	//  Geometry — where the recorded content sits and its transform origin.

	var topLeft: IntOffset
	var size: IntSize
	var pivot: Offset

	// ============
	//  Transform — applied at replay, no re-record (matches SkiaGraphicsLayer).

	var alpha: Float
	var scaleX: Float
	var scaleY: Float
	var translationX: Float
	var translationY: Float
	var rotationX: Float
	var rotationY: Float
	var rotationZ: Float
	var cameraDistance: Float

	// ============
	//  Elevation shadow (ARGB ints, mirroring skiko RenderNode).

	var shadowElevation: Float
	var ambientShadowColor: Int
	var spotShadowColor: Int

	// ============
	//  Clip — outline pushed onto the node; rect / rounded-rect / generic path.

	var clip: Boolean
	fun setClipRect(left: Float, top: Float, right: Float, bottom: Float, antiAlias: Boolean = true)
	fun setClipRRect(left: Float, top: Float, right: Float, bottom: Float, radii: FloatArray, antiAlias: Boolean = true)
	fun setClipPath(path: Path?, antiAlias: Boolean = true)

	// ============
	//  Layer paint — the saveLayer paint carrying alpha / colorFilter / blendMode /
	//  imageFilter when the layer needs an offscreen (requiresLayer()); null = draw
	//  in place. Provisional type — see NOTE above.

	var layerPaint: Paint?

	// ============
	//  Record / replay.

	/** Begin recording; draw the layer content into the returned canvas. */
	fun beginRecording(): Canvas

	/** Finish the current recording; the display list is now replayable. */
	fun endRecording()

	/** Replay the recorded display list onto [canvas], applying this node's transform. */
	fun drawInto(canvas: Canvas)

	/** Release native resources (textures / picture / display list). */
	fun close()
}

/**
 * Per-window context shared by the [NativeRenderNode]s of one composition — the
 * analogue of skiko's `RenderNodeContext`. Carries renderer-wide settings (and,
 * later, shadow lighting + the renderer handle the SDL node needs to allocate
 * offscreen targets). Kept minimal at Phase 0.
 */
internal class NativeRenderNodeContext(
	val measureDrawBounds: Boolean = false,
)

/**
 * Create a [NativeRenderNode] for the active renderer. One actual per renderer
 * source set (skikoRenderer / sdlRenderer); exactly one is attached per target, so
 * resolution is unambiguous — same pattern as [createRenderBackend]. Declared here
 * so shared nativeMain metadata is self-contained (CLAUDE.md source-set notes).
 */
internal expect fun createNativeRenderNode(context: NativeRenderNodeContext): NativeRenderNode
