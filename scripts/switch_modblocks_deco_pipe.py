import re
from pathlib import Path

p = Path("src/main/java/com/hbm/blocks/ModBlocks.java")
src = p.read_text(encoding="utf-8")

imports_to_add = [
    "import com.hbm.blocks.generic.BlockPipe;",
    "import com.hbm.blocks.ModSoundTypes;",
]
for imp in imports_to_add:
    if imp not in src:
        src = re.sub(r"(\nimport [^\n]+;\n)(?!\nimport)", r"\1" + imp + "\n", src, count=1)

# 6 colors × 4 rTypes = 24 fields
COLORS = {
    "":              "hbm:pipe_side",
    "_rusted":       "hbm:pipe_side_rusty",
    "_green":        "hbm:pipe_side_green",
    "_green_rusted": "hbm:pipe_side_green_rusty",
    "_red":          "hbm:pipe_side_red",
    "_marked":       "hbm:pipe_side_marked",
}
RTYPES = {
    "":         0,
    "_rim":     1,
    "_framed":  3,
    "_quad":    2,
}

fields = []
for rtype_suffix, rtype_id in RTYPES.items():
    for color_suffix, tex in COLORS.items():
        field = f"deco_pipe{rtype_suffix}{color_suffix}"
        ctor = f'new BlockPipe(Material.IRON, ModSoundTypes.grate, "{tex}", {rtype_id})'
        fields.append((field, ctor))

old_ctor_template = "new Block(Material.IRON)"
for field, new_ctor in fields:
    pattern = rf"({re.escape(field)}\s*=\s*){re.escape(old_ctor_template)}"
    new_text, n = re.subn(pattern, r"\1" + new_ctor, src)
    if n != 1:
        raise SystemExit(f"FAILED: {field} matched {n} times (expected 1)")
    src = new_text

p.write_text(src, encoding="utf-8")
print(f"OK: {len(fields)} deco_pipe fields swapped to BlockPipe")
