import re
from pathlib import Path

p = Path("src/main/java/com/hbm/blocks/ModBlocks.java")
src = p.read_text(encoding="utf-8")

imports_to_add = [
    "import com.hbm.blocks.generic.WasteLog;",
    "import com.hbm.blocks.generic.WasteLeaves;",
    "import com.hbm.blocks.generic.BlockStepEffect;",
]
for imp in imports_to_add:
    if imp not in src:
        src = re.sub(r"(\nimport [^\n]+;\n)(?!\nimport)", r"\1" + imp + "\n", src, count=1)

# Each tuple: (field_name, old_ctor_pattern, new_ctor)
# Replaces only "new Block(Material.X)" at the start; the trailing .setUnlocalizedName(...) chain is preserved.
field_swaps = [
    ("waste_log",           r"new Block\(Material\.WOOD\)",   "new WasteLog(Material.WOOD)"),
    ("waste_leaves",        r"new Block\(Material\.LEAVES\)", "new WasteLeaves(Material.LEAVES)"),
    ("waste_trinitite",     r"new Block\(Material\.SAND\)",   "new BlockStepEffect(Material.SAND).noFortune()"),
    ("waste_trinitite_red", r"new Block\(Material\.SAND\)",   "new BlockStepEffect(Material.SAND).noFortune()"),
    ("waste_planks",        r"new Block\(Material\.WOOD\)",   "new BlockStepEffect(Material.WOOD)"),
]

for field, old_pat, new_ctor in field_swaps:
    pattern = rf"({re.escape(field)}\s*=\s*){old_pat}"
    new_text, n = re.subn(pattern, r"\1" + new_ctor, src)
    if n != 1:
        raise SystemExit(f"FAILED: {field} matched {n} times (expected 1)")
    src = new_text

p.write_text(src, encoding="utf-8")
print("OK: 5 fields swapped")
