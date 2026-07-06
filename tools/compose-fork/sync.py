#!/usr/bin/env python3
# Vendor pure, platform-independent leaf files from JetBrains/compose-multiplatform-core,
# BYTE-FOR-BYTE VERBATIM (no edits, no reformatting).
#
# This is the real implementation behind `sync.sh` (which is now a thin wrapper).
# It is a straight port of the old bash script, but does the ~1500-file copy loop
# IN-PROCESS instead of spawning ~7 subprocesses (dirname/mkdir/cp/grep/grep/sed/mv)
# per file. On Windows Git Bash a subprocess spawn costs ~100ms (antivirus scans
# every spawned .exe), so the bash version took 15-25 minutes and could wedge on a
# tmp-file rename; this runs in a couple of seconds and never touches temp files.
# Pure Python + a handful of `git` calls -> works the same on macOS / Linux / Windows.
#
# Each Gradle module that vendors upstream code carries its own `compose-fork.txt`
# alongside its `build.gradle.kts`, listing upstream files vendored into that module.
# `sync.py` walks every `*/compose-fork.txt` (or just the ones you ask for), copies
# each listed upstream file to the dest path shown next to it (relative to the
# manifest's own directory).
#
# Idempotent -- re-run after bumping compose-ref.txt to re-sync. The copy is verbatim,
# so `git diff` against a fresh upstream checkout shows exactly what upstream changed.
# Provenance = manifest + compose-ref.txt -- do NOT hand-edit vendored files; change a
# manifest or the ref and re-run instead.
#
# Usage (any platform):
#   python tools/compose-fork/sync.py                              # every module with a compose-fork.txt
#   python tools/compose-fork/sync.py :compose:animation-core      # gradle path
#   python tools/compose-fork/sync.py compose/animation-core       # module path
#   python tools/compose-fork/sync.py compose/ui/compose-fork.txt  # direct path to a manifest
# Env:
#   CMP_REF=<path>   reuse/create the clone here (default ../cmp-ref)

import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
REPO_URL = 'https://github.com/JetBrains/compose-multiplatform-core'
CMP_REF = os.environ.get('CMP_REF') or os.path.normpath(os.path.join(REPO_ROOT, '..', 'cmp-ref'))

# Dirs we never descend into when auto-discovering manifests.
PRUNE_DIRS = {'build', '.gradle', '.git', 'node_modules', 'tools'}

# `compose/<area>/<module>` prefix at the start of an upstream path -- one per
# sparse-checkout dir.
MODULE_RE = re.compile(r'compose/[a-z0-9-]+/[a-z0-9-]+')

# Kotlin 2.4's K2 metadata compile rejects two upstream-Compose idioms that were fine
# in 2.3 (OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE for JvmField/JvmName/JvmStatic
# imports in commonMain, and LESS_VISIBLE_TYPE_ACCESS_IN_INLINE for the Synchronization
# helpers). We suppress both file-level on every vendored .kt so the metadata publish
# succeeds without hand-editing files (the vendor tree is regenerated and must carry no
# local edits). The marker below is what tells us a file is already suppressed.
SUPPRESS_MARKER = b'OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE'
SUPPRESS_NAMES = b'"OPTIONAL_DECLARATION_USAGE_IN_NON_COMMON_SOURCE", "LESS_VISIBLE_TYPE_ACCESS_IN_INLINE"'
FILE_SUPPRESS_RE = re.compile(rb'(?m)^@file:Suppress\(')


# ============
#  Resolve one CLI arg to a manifest path. Accepts:
#    :foo:bar          -> foo/bar/compose-fork.txt
#    foo:bar           -> foo/bar/compose-fork.txt
#    foo/bar           -> foo/bar/compose-fork.txt
#    foo               -> foo/compose-fork.txt
#    foo/compose-fork.txt (or any *.txt path, absolute or relative to repo root)
def resolve_manifest(arg):
	if arg.endswith('.txt'):
		return arg if os.path.isabs(arg) else os.path.join(REPO_ROOT, arg)
	rel = arg.lstrip(':').replace(':', '/')
	return os.path.join(REPO_ROOT, rel, 'compose-fork.txt')


# ============
#  Every `<module>/compose-fork.txt` under the repo, skipping build/tool outputs.
def find_all_manifests():
	found = []
	for dirpath, dirs, files in os.walk(REPO_ROOT):
		dirs[:] = [d for d in dirs if d not in PRUNE_DIRS]
		if 'compose-fork.txt' in files:
			found.append(os.path.join(dirpath, 'compose-fork.txt'))
	return sorted(found)


# ============
#  Active (uncommented) `<up> <dest>` pairs from a manifest's text.
def active_entries(text):
	for line in text.splitlines():
		s = line.strip()
		if not s or s.startswith('#'):
			continue
		parts = s.split()
		if len(parts) >= 2:
			yield parts[0], parts[1]


# ============
#  Union of `compose/<area>/<module>` prefixes referenced by a manifest -- ACTIVE and
#  COMMENTED alike (a leading `#` is stripped first, matching the old bash behaviour).
#  Computed across EVERY manifest so a partial sync never shrinks the sparse clone.
def sparse_prefixes(text):
	prefixes = set()
	for line in text.splitlines():
		s = line.strip().lstrip('#').strip()
		tok = s.split(None, 1)[0] if s else ''
		m = MODULE_RE.match(tok)
		if m:
			prefixes.add(m.group(0))
	return prefixes


# ============
#  Inject the K2 @file:Suppress into vendored .kt content, byte-for-byte identical to
#  what the old script produced. Returns the (possibly rewritten) bytes.
def inject_suppress(data):
	if SUPPRESS_MARKER in data:
		return data  # already suppressed -- leave untouched
	if FILE_SUPPRESS_RE.search(data):
		# Splice our names in right after `@file:Suppress(`, keeping the rest of the line.
		return FILE_SUPPRESS_RE.sub(b'@file:Suppress(' + SUPPRESS_NAMES + b', ', data)
	# No file-level suppress yet -- prepend a fresh one.
	return b'@file:Suppress(' + SUPPRESS_NAMES + b')\n\n' + data


# ============
#  git helpers (a handful of calls total -- not per file).
def git(args, quiet=False, check=True):
	kw = {}
	if quiet:
		kw['stdout'] = subprocess.DEVNULL
		kw['stderr'] = subprocess.DEVNULL
	return subprocess.run(['git', '-C', CMP_REF, *args], check=check, **kw)


def ensure_clone(sparse_dirs, ref):
	if not os.path.isdir(os.path.join(CMP_REF, '.git')):
		print('cloning %s -> %s (%d sparse dirs)' % (REPO_URL, CMP_REF, len(sparse_dirs)))
		# Pin autocrlf/eol off on the clone so the working tree holds upstream's exact
		# LF bytes -- otherwise a machine with core.autocrlf=true (Windows default)
		# checks files out as CRLF and we'd vendor mangled line endings. We also
		# normalize on write below as a belt-and-suspenders for pre-existing clones.
		subprocess.run(['git', 'clone', '--filter=blob:none', '--no-checkout',
			'--config', 'core.autocrlf=false', '--config', 'core.eol=lf', REPO_URL, CMP_REF], check=True)
		git(['sparse-checkout', 'set', *sparse_dirs])
	else:
		# Keep an existing clone's line-ending config sane too (helps future checkouts).
		git(['config', 'core.autocrlf', 'false'], quiet=True, check=False)
		git(['config', 'core.eol', 'lf'], quiet=True, check=False)
		# Extend the sparse set in case a new manifest reaches into a new area. Noop if covered.
		git(['sparse-checkout', 'set', *sparse_dirs], quiet=True)
	if git(['checkout', '-q', ref], quiet=True, check=False).returncode != 0:
		print('ref %s not present locally -- fetching' % ref)
		git(['fetch', 'origin', ref])
		git(['checkout', '-q', ref])
	desc = subprocess.run(['git', '-C', CMP_REF, 'describe', '--tags', '--always'],
		capture_output=True, text=True).stdout.strip()
	print('upstream @ %s' % desc)


def read_ref():
	with open(os.path.join(HERE, 'compose-ref.txt'), encoding='utf-8') as f:
		for line in f:
			s = line.strip()
			if s and not s.startswith('#'):
				return s
	sys.stderr.write('no ref in compose-ref.txt\n')
	sys.exit(1)


def main():
	args = sys.argv[1:]
	ref = read_ref()

	# ---- select manifests (all, or the ones named on the CLI)
	if args:
		manifests = []
		for arg in args:
			f = resolve_manifest(arg)
			if not os.path.isfile(f):
				sys.stderr.write('no such manifest: %s\n  (from arg %r)\n' % (f, arg))
				sys.exit(1)
			manifests.append(f)
	else:
		manifests = find_all_manifests()
		if not manifests:
			sys.stderr.write('no <module>/compose-fork.txt found under %s\n' % REPO_ROOT)
			sys.exit(1)

	# ---- sparse set = union across ALL manifests (partial sync must not shrink the clone)
	sparse = set()
	for m in find_all_manifests():
		with open(m, encoding='utf-8') as f:
			sparse |= sparse_prefixes(f.read())
	sparse_dirs = sorted(sparse)

	# ---- 1. sparse clone at the pinned ref
	ensure_clone(sparse_dirs, ref)

	# ---- 1.5 canonicalize + discover each selected manifest (non-fatal)
	fmt = os.path.join(HERE, 'format-manifest.py')
	if os.path.isfile(fmt):
		for m in manifests:
			if subprocess.run([sys.executable, fmt, '--discover', CMP_REF, '--manifest', m]).returncode != 0:
				sys.stderr.write('warn: format-manifest.py failed on %s -- continuing\n' % m)

	# ---- 2. copy each manifest's entries verbatim (dest relative to the manifest's dir)
	total = 0
	for m in manifests:
		module_dir = os.path.dirname(m)
		label = os.path.relpath(module_dir, REPO_ROOT).replace(os.sep, '/')
		count = 0
		with open(m, encoding='utf-8') as f:
			text = f.read()
		for up, dest in active_entries(text):
			src = os.path.join(CMP_REF, *up.split('/'))
			if not os.path.isfile(src):
				sys.stderr.write('MISSING upstream file: %s\n' % up)
				sys.exit(1)
			dst = os.path.join(module_dir, *dest.split('/'))
			os.makedirs(os.path.dirname(dst), exist_ok=True)
			with open(src, 'rb') as fh:
				data = fh.read()
			if dest.endswith('.kt'):
				# Normalize to LF (upstream's real bytes) so a CRLF working tree from
				# core.autocrlf can't leak mangled line endings into the vendor tree.
				data = data.replace(b'\r\n', b'\n')
			if dest.startswith('src/vendor/') and dest.endswith('.kt'):
				data = inject_suppress(data)
			with open(dst, 'wb') as fh:
				fh.write(data)
			count += 1
		print('  %s: %d files' % (label, count))
		total += count

	print('synced %d files verbatim at %s' % (total, ref))


if __name__ == '__main__':
	main()
