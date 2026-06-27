# `core/src/vendor` — vendored upstream Compose (DO NOT EDIT)

Every file under this folder is copied **byte-for-byte verbatim** from
[JetBrains/compose-multiplatform-core](https://github.com/JetBrains/compose-multiplatform-core)
by `tools/compose-fork/sync.sh`. These are the pure, platform-independent leaf
types (math / value classes / colorspace) that we mirror exactly instead of
hand-porting.

**Do not hand-edit anything in here.** To change it:

- update the pin in `tools/compose-fork/compose-ref.txt`, and/or
- add/remove entries in `tools/compose-fork/manifest.txt`, then
- run `tools/compose-fork/sync.sh`.

Because the copy is verbatim, `git diff` after a re-sync shows exactly what
upstream changed. Engine / renderer / widget code and any non-official extras
(e.g. `ColorExtensions.kt`, the `TileMode.isSupported()` actual) are **hand-written
and live in the normal `src/commonMain` / `src/nativeMain` trees** — never here.

Layout:
- `common/kotlin/` → registered into the `commonMain` source set
- `native/kotlin/`  → registered into the `nativeMain` source set (platform `actual`s)
