import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toKString

// Current resident set (MB) via `ps -o rss=` (KB). See currentResidentMb in MainNative.kt.
@OptIn(ExperimentalForeignApi::class)
internal actual fun currentResidentMb(): Long {
    val vPid = platform.posix.getpid()
    val vFp = platform.posix.popen("ps -o rss= -p $vPid", "r") ?: return -1
    val vKb = memScoped {
        val vBuf = allocArray<kotlinx.cinterop.ByteVar>(64)
        val vLine = platform.posix.fgets(vBuf, 64, vFp)?.toKString()?.trim()
        vLine?.toLongOrNull() ?: -1L
    }
    platform.posix.pclose(vFp)
    return if (vKb < 0) -1 else vKb / 1024
}
