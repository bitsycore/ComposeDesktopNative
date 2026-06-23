package com.compose.desktop.native

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.SEEK_END
import platform.posix.SEEK_SET
import platform.posix.FILE
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fread
import platform.posix.fseek
import platform.posix.ftell
import platform.zlib.Z_FINISH
import platform.zlib.Z_OK
import platform.zlib.Z_STREAM_END
import platform.zlib.inflate
import platform.zlib.inflateEnd
import platform.zlib.inflateInit2_
import platform.zlib.uByteVar
import platform.zlib.z_stream
import platform.zlib.zlibVersion
import sdl3.SDL_GetBasePath

// ==================
// MARK: composeResources archive IO
// ==================
// Resources are bundled in a single zip "data.kres" next to the executable
// by the demo's Gradle Zip task (".kres" = a zip with a project-specific
// extension so the bundle reads as a single opaque blob next to the binary).
// At runtime we open the archive once via SDL_GetBasePath(), parse its
// central directory, then serve each resource by fseek+fread per entry —
// no whole-archive memory load.
//
// Supports STORED (method 0) and DEFLATED (method 8) entries. Deflated
// entries are inflated on read via the system zlib (raw deflate stream,
// no zlib/gzip wrapper). ZIP64 is not supported — the resource set is
// small enough that the standard 4 GB / 65535-entry limits don't apply.

@OptIn(ExperimentalForeignApi::class)
private class ComposeResourceArchive(private val fFile: CPointer<FILE>) {

	// One entry's location + sizing + compression method from the central
	// directory. method 0 = stored, 8 = deflated.
	private class Entry(
		val localOffset: Long,
		val compressedSize: Long,
		val uncompressedSize: Long,
		val method: Int,
	)
	private val fEntries: Map<String, Entry>

	init {
		fEntries = readCentralDirectory()
	}

	fun has(inPath: String): Boolean = fEntries.containsKey(inPath)

	/* Reads an entry's bytes (inflated if needed) or null if the entry is
	   missing / uses an unsupported compression method. */
	fun readBytes(inPath: String): ByteArray? {
		val vEntry = fEntries[inPath] ?: return null
		if (vEntry.uncompressedSize <= 0L) return ByteArray(0)

		// Local file header layout: 30 fixed bytes, then filename_len + extra_len.
		// The CD's extra field can differ in size from the local extra field
		// (e.g. when a writer adds an extra to only one), so always read the local
		// header's lengths.
		val vHeader = ByteArray(30)
		if (!seekAndRead(vEntry.localOffset, vHeader)) return null
		if (le32(vHeader, 0) != 0x04034b50) return null
		val vNameLen = le16(vHeader, 26)
		val vExtraLen = le16(vHeader, 28)
		val vDataOffset = vEntry.localOffset + 30L + vNameLen + vExtraLen

		val vRaw = ByteArray(vEntry.compressedSize.toInt())
		if (!seekAndRead(vDataOffset, vRaw)) return null

		return when (vEntry.method) {
			0 -> vRaw
			8 -> inflateRawDeflate(vRaw, vEntry.uncompressedSize.toInt())
			else -> null
		}
	}

	/* Decompress a raw-deflate stream (no zlib/gzip header) into a buffer
	   of the known uncompressed length. windowBits = -MAX_WBITS (-15) is
	   what zlib calls "raw deflate" — matches the zip entry payload. */
	private fun inflateRawDeflate(inRaw: ByteArray, inOutLen: Int): ByteArray? {
		val vOut = ByteArray(inOutLen)
		memScoped {
			val vStream = alloc<z_stream>()
			vStream.zalloc = null; vStream.zfree = null; vStream.opaque = null
			// inflateInit2_ is the size+version-checked entry point that the
			// inflateInit2 macro expands to; we pass it directly because the
			// macro isn't visible through cinterop.
			val vInit = inflateInit2_(vStream.ptr, -15, zlibVersion()?.toKString(), sizeOf<z_stream>().toInt())
			if (vInit != Z_OK) return@memScoped
			try {
				inRaw.usePinned { vIn ->
					vOut.usePinned { vOutPin ->
						vStream.next_in = vIn.addressOf(0).reinterpret()
						vStream.avail_in = inRaw.size.convert()
						vStream.next_out = vOutPin.addressOf(0).reinterpret()
						vStream.avail_out = inOutLen.convert()
						val vR = inflate(vStream.ptr, Z_FINISH)
						if (vR != Z_STREAM_END && vR != Z_OK) return@memScoped
					}
				}
			} finally { inflateEnd(vStream.ptr) }
		}
		return vOut
	}

	// ==================
	// MARK: Central directory parse
	// ==================

	private fun readCentralDirectory(): Map<String, Entry> {
		// End of Central Directory record: 22 bytes minimum, with up to 65535
		// bytes of comment trailing. Read the last 64 KiB + 22 and scan backward
		// for the EOCD signature.
		fseek(fFile, 0.convert(), SEEK_END)
		val vFileLen: Long = ftell(fFile).convert()
		if (vFileLen < 22L) return emptyMap()
		val vTailLen = minOf(vFileLen, 65557L).toInt()
		val vTail = ByteArray(vTailLen)
		val vTailStart = vFileLen - vTailLen
		if (!seekAndRead(vTailStart, vTail)) return emptyMap()

		var vEocd = -1
		var i = vTailLen - 22
		while (i >= 0) {
			if (le32(vTail, i) == 0x06054b50) { vEocd = i; break }
			i--
		}
		if (vEocd < 0) return emptyMap()

		val vCdSize = le32(vTail, vEocd + 12).toLong() and 0xFFFFFFFFL
		val vCdOffset = le32(vTail, vEocd + 16).toLong() and 0xFFFFFFFFL
		if (vCdSize <= 0L || vCdOffset < 0L) return emptyMap()

		val vCd = ByteArray(vCdSize.toInt())
		if (!seekAndRead(vCdOffset, vCd)) return emptyMap()

		val vMap = HashMap<String, Entry>()
		var vP = 0
		while (vP + 46 <= vCd.size) {
			if (le32(vCd, vP) != 0x02014b50) break
			val vMethod = le16(vCd, vP + 10)
			val vCompressed = le32(vCd, vP + 20).toLong() and 0xFFFFFFFFL
			val vUncompressed = le32(vCd, vP + 24).toLong() and 0xFFFFFFFFL
			val vNameLen = le16(vCd, vP + 28)
			val vExtraLen = le16(vCd, vP + 30)
			val vCommentLen = le16(vCd, vP + 32)
			val vLocalOffset = le32(vCd, vP + 42).toLong() and 0xFFFFFFFFL

			if (vP + 46 + vNameLen > vCd.size) break
			val vName = vCd.decodeToString(vP + 46, vP + 46 + vNameLen)

			// Accept stored + deflated; skip directory placeholders (trailing '/').
			if ((vMethod == 0 || vMethod == 8) && !vName.endsWith("/")) {
				vMap[vName] = Entry(vLocalOffset, vCompressed, vUncompressed, vMethod)
			}
			vP += 46 + vNameLen + vExtraLen + vCommentLen
		}
		return vMap
	}

	// ==================
	// MARK: stdio helpers
	// ==================

	private fun seekAndRead(inOffset: Long, outBuf: ByteArray): Boolean {
		if (fseek(fFile, inOffset.convert(), SEEK_SET) != 0) return false
		if (outBuf.isEmpty()) return true
		return outBuf.usePinned { vPinned ->
			fread(vPinned.addressOf(0), 1.convert(), outBuf.size.convert(), fFile).toInt() == outBuf.size
		}
	}

	private fun le16(inBuf: ByteArray, inOff: Int): Int =
		(inBuf[inOff].toInt() and 0xFF) or
		((inBuf[inOff + 1].toInt() and 0xFF) shl 8)

	private fun le32(inBuf: ByteArray, inOff: Int): Int =
		(inBuf[inOff].toInt() and 0xFF) or
		((inBuf[inOff + 1].toInt() and 0xFF) shl 8) or
		((inBuf[inOff + 2].toInt() and 0xFF) shl 16) or
		((inBuf[inOff + 3].toInt() and 0xFF) shl 24)
}

// ==================
// MARK: Lazy singleton
// ==================

@OptIn(ExperimentalForeignApi::class)
private val kArchive: ComposeResourceArchive? by lazy {
	val vBase = SDL_GetBasePath()?.toKString() ?: return@lazy null
	if (vBase.isEmpty()) return@lazy null
	val vPath = vBase + "data.kres"
	val vFile = fopen(vPath, "rb") ?: run {
		println("ComposeResourceArchive: not found at $vPath")
		return@lazy null
	}
	ComposeResourceArchive(vFile)
}

// ==================
// MARK: Public API
// ==================

/* Lets an app feed the image pipeline bytes it produced at runtime (e.g. a
   downloaded PNG) under a synthetic path, so painterResource(key) / Image(...)
   render it through the same decode + cache path as bundled drawables. Use a
   unique key per distinct content — renderers cache decoded textures by path,
   so reusing a key keeps showing the first bytes. */
private val kMemoryResources = mutableMapOf<String, ByteArray>()

fun registerMemoryResource(inKey: String, inBytes: ByteArray) { kMemoryResources[inKey] = inBytes }

fun removeMemoryResource(inKey: String) { kMemoryResources.remove(inKey) }

/* Reads a resource's raw bytes — an in-memory resource if registered under the
   path, otherwise the bundled entry inside data.kres. Null when neither exists. */
@OptIn(ExperimentalForeignApi::class)
fun loadComposeResourceBytes(inRelativePath: String): ByteArray? =
	kMemoryResources[inRelativePath] ?: kArchive?.readBytes(inRelativePath)

/* True when an entry exists in memory or the bundled archive. Cheap. */
@OptIn(ExperimentalForeignApi::class)
fun hasComposeResource(inRelativePath: String): Boolean =
	kMemoryResources.containsKey(inRelativePath) || kArchive?.has(inRelativePath) == true
