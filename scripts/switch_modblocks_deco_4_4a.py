import re
from pathlib import Path

p = Path("src/main/java/com/hbm/blocks/ModBlocks.java")
src = p.read_text(encoding="utf-8")

imports_to_add = [
    "import com.hbm.blocks.generic.BlockDecoCT;",
]
for imp in imports_to_add:
    if imp not in src:
        src = re.sub(r"(\nimport [^\n]+;\n)(?!\nimport)", r"\1" + imp + "\n", src, count=1)

# deco_titanium: BlockOre has no noFortune() in port, use BlockDecoCT.noFortune() as fallback
field_swaps = [
    ("deco_titanium",    "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_red_copper",  "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_tungsten",    "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_aluminium",   "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_steel",       "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_rusty_steel", "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_lead",        "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_beryllium",   "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON).noFortune()"),
    ("deco_stainless",   "new Block(Material.IRON)", "new BlockDecoCT(Material.IRON)"),
]

for field, old_ctor, new_ctor in field_swaps:
    pattern = rf"({re.escape(field)}\s*=\s*){re.escape(old_ctor)}"
    new_text, n = re.subn(pattern, r"\1" + new_ctor, src)
    if n != 1:
        raise SystemExit(f"FAILED: {field} matched {n} times (expected 1)")
    src = new_text

p.write_text(src, encoding="utf-8")
print("OK: 9 deco-metal fields swapped to BlockDecoCT")
