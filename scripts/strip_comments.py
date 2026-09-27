#!/usr/bin/env python3
import re
import sys
from pathlib import Path


def strip_kotlin(src: str) -> str:
    out = []
    n = len(src)
    i = 0
    modes = ["code"]
    brace_depth = []

    def emit(s):
        out.append(s)

    while i < n:
        mode = modes[-1]
        c = src[i]
        if mode == "code":
            if src.startswith("//", i):
                j = src.find("\n", i)
                i = n if j == -1 else j
                continue
            if src.startswith("/*", i):
                depth = 1
                i += 2
                while i < n and depth:
                    if src.startswith("/*", i):
                        depth += 1
                        i += 2
                    elif src.startswith("*/", i):
                        depth -= 1
                        i += 2
                    else:
                        i += 1
                if out and out[-1] and not out[-1][-1].isspace() and i < n and not src[i].isspace():
                    emit(" ")
                continue
            if src.startswith('"""', i):
                emit('"""')
                i += 3
                modes.append("raw")
                continue
            if c == '"':
                emit(c)
                i += 1
                modes.append("str")
                continue
            if c == "'":
                m = re.match(r"'(?:\\u[0-9a-fA-F]{4}|\\.|[^\\'\n])'", src[i:i + 12])
                if m:
                    emit(m.group(0))
                    i += len(m.group(0))
                    continue
                emit(c)
                i += 1
                continue
            if c == "{":
                if len(modes) > 1 and brace_depth:
                    brace_depth[-1] += 1
                emit(c)
                i += 1
                continue
            if c == "}":
                if brace_depth and brace_depth[-1] == 0:
                    brace_depth.pop()
                    modes.pop()
                    emit(c)
                    i += 1
                    continue
                if brace_depth:
                    brace_depth[-1] -= 1
                emit(c)
                i += 1
                continue
            emit(c)
            i += 1
        elif mode == "str":
            if c == "\\":
                emit(src[i:i + 2])
                i += 2
                continue
            if c == '"':
                emit(c)
                i += 1
                modes.pop()
                continue
            if src.startswith("${", i):
                emit("${")
                i += 2
                modes.append("code")
                brace_depth.append(0)
                continue
            emit(c)
            i += 1
        elif mode == "raw":
            if src.startswith('"""', i):
                j = i
                while j < n and src[j] == '"':
                    j += 1
                emit(src[i:j])
                i = j
                modes.pop()
                continue
            if src.startswith("${", i):
                emit("${")
                i += 2
                modes.append("code")
                brace_depth.append(0)
                continue
            emit(c)
            i += 1
    return "".join(out)


def tidy(original: str, stripped: str) -> str:
    orig_lines = original.split("\n")
    new_lines = stripped.split("\n")
    result = []
    if len(orig_lines) == len(new_lines):
        for o, s in zip(orig_lines, new_lines):
            if o.strip() and not s.strip():
                continue
            result.append(s.rstrip() if o != s else s)
    else:
        for s in new_lines:
            result.append(s.rstrip())
    text = "\n".join(result)
    text = re.sub(r"\n{3,}", "\n\n", text)
    return text.rstrip("\n") + "\n"


def strip_xml(src: str) -> str:
    text = re.sub(r"[ \t]*<!--.*?-->[ \t]*\n?", "", src, flags=re.S)
    text = re.sub(r"\n{3,}", "\n\n", text)
    if not text.endswith("\n"):
        text += "\n"
    return text


def strip_file(path: Path) -> None:
    src = path.read_text(encoding="utf-8")
    if path.suffix in (".kt", ".kts"):
        result = tidy(src, strip_kotlin(src))
    elif path.suffix == ".xml":
        result = strip_xml(src)
    else:
        return
    if result != src:
        path.write_text(result, encoding="utf-8")


if __name__ == "__main__":
    for arg in sys.argv[1:]:
        p = Path(arg)
        files = [p] if p.is_file() else [f for f in p.rglob("*") if f.is_file() and f.suffix in (".kt", ".kts", ".xml")]
        for f in files:
            strip_file(f)
