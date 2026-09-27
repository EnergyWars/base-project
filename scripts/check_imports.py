#!/usr/bin/env python3
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
DECL = re.compile(
    r"^((?:(?:public|internal|private|protected|open|abstract|sealed|data|enum|annotation|inline|value|fun|const|lateinit|suspend|operator|infix|tailrec|external|expect|actual|override)\s+)*)"
    r"(class|interface|object|typealias|fun|val|var)\s+(?:<(?:[^<>]|<[^<>]*>)*>\s*)?(?:[\w<>?,.\s*]+?\.)?(`?\w+`?)"
)


def index_sources(roots):
    symbols = {}
    for root in roots:
        for f in root.rglob("*.kt"):
            if "/build/" in str(f):
                continue
            text = f.read_text(encoding="utf-8")
            m = re.search(r"^package\s+([\w.]+)", text, re.M)
            if not m:
                continue
            pkg = m.group(1)
            bucket = symbols.setdefault(pkg, {})
            for line in text.split("\n"):
                d = DECL.match(line)
                if not d:
                    continue
                mods, _, name = d.groups()
                name = name.strip("`")
                visibility = "internal" if "internal" in mods else "private" if "private" in mods else "public"
                if visibility == "private":
                    continue
                cur = bucket.get(name)
                if cur is None or (cur == "internal" and visibility == "public"):
                    bucket[name] = visibility
    return symbols


def main():
    targets = [Path(a) for a in sys.argv[1:]] or [ROOT / "app" / "src"]
    lib_roots = [p for p in (ROOT / "lib").glob("*/src/main")]
    app_roots = [ROOT / "app" / "src" / "main"]
    test_roots = list((ROOT / "app" / "src").glob("test")) + list((ROOT / "lib").glob("*/src/test"))
    all_symbols = index_sources(lib_roots + app_roots + test_roots)
    lib_pkgs = set(index_sources(lib_roots).keys())
    problems = 0
    for target in targets:
        for f in target.rglob("*.kt"):
            text = f.read_text(encoding="utf-8")
            pm = re.search(r"^package\s+([\w.]+)", text, re.M)
            own_pkg = pm.group(1) if pm else ""
            own_module = None
            parts = f.parts
            if "lib" in parts:
                own_module = parts[parts.index("lib") + 1]
            for im in re.finditer(r"^import\s+([\w.]+?)(?:\s+as\s+\w+)?\s*$", text, re.M):
                imp = im.group(1)
                if imp.endswith(".*"):
                    continue
                if not imp.startswith("com.wafflehq."):
                    continue
                segs = imp.split(".")
                if "R" in segs and segs[segs.index("R") - 1] in ("uicore", "settings", "navigation", "base", "folders", "entrylock", "quickpicker", "pdf", "textarea", "modules", "maintenance", "media", "notifications", "prefsbackup", "database", "diagnostics", "charts", "drafts", "qr", "astronomy", "backupcore"):
                    continue
                found = False
                for cut in range(len(segs) - 1, 0, -1):
                    pkg = ".".join(segs[:cut])
                    name = segs[cut]
                    if pkg in all_symbols and name in all_symbols[pkg]:
                        found = True
                        vis = all_symbols[pkg][name]
                        sym_module = pkg.split(".")[3].replace("-", "") if pkg.startswith("com.wafflehq.lib.") and len(pkg.split(".")) > 3 else None
                        if vis == "internal" and sym_module and (own_module or "app").replace("-", "") != sym_module:
                            print(f"{f}: import of internal symbol {imp}")
                            problems += 1
                        break
                if not found:
                    print(f"{f}: unresolved import {imp}")
                    problems += 1
    print(f"{problems} problem(s)")
    sys.exit(1 if problems else 0)


if __name__ == "__main__":
    main()
