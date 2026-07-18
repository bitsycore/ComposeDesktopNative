// Windows has no `ps`/popen; the memory soak (--soaktest) is a macOS/Linux gate
// (see scripts/verify-mac.sh), so current RSS is not sampled here.
internal actual fun currentResidentMb(): Long = -1L
