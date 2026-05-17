import re
from pathlib import Path

p = Path("src/main/java/com/hbm/blocks/ModBlocks.java")
src = p.read_text(encoding="utf-8")

imports_to_add = [
    "import com.hbm.blocks.generic.BlockDecoCRT;",
    "import com.hbm.blocks.generic.BlockDecoToaster;",
    "import com.hbm.blocks.generic.BlockDecoModel;",
    "import com.hbm.blocks.BlockEnums;",
]
for imp in imports_to_add:
    if imp not in src:
        src = re.sub(r"(\nimport [^\n]+;\n)(?!\nimport)", r"\1" + imp + "\n", src, count=1)

# Block ctor swaps: replace only the ctor part, preserve the rest of the chain
ctor_swaps = [
    ("deco_crt",      "new Block(Material.IRON)", "new BlockDecoCRT(Material.IRON)"),
    ("deco_toaster",  "new Block(Material.IRON)", "new BlockDecoToaster(Material.IRON)"),
    ("deco_computer", "new Block(Material.IRON)",
     "new BlockDecoModel(Material.IRON, BlockEnums.DecoComputerEnum.class, true)"
     ".setBlockBoundsTo(0.125F, 0F, 0F, 0.875F, 0.875F, 0.625F)"),
]
for field, old, new in ctor_swaps:
    pattern = rf"({re.escape(field)}\s*=\s*){re.escape(old)}"
    new_src, n = re.subn(pattern, r"\1" + new, src)
    if n != 1:
        raise SystemExit(f"FAILED: {field} matched {n} times (expected 1)")
    src = new_src

# ItemBlock → ItemBlockEnumMulti swaps
ib_swaps = [
    ("new ItemBlock(deco_computer)", "new ItemBlockEnumMulti(deco_computer)"),
    ("new ItemBlock(deco_crt)",      "new ItemBlockEnumMulti(deco_crt)"),
    ("new ItemBlock(deco_toaster)",  "new ItemBlockEnumMulti(deco_toaster)"),
]
for old, new in ib_swaps:
    count = src.count(old)
    if count != 1:
        raise SystemExit(f"FAILED: '{old}' matched {count} times (expected 1)")
    src = src.replace(old, new)

p.write_text(src, encoding="utf-8")
print("OK: 3 fields + 3 ItemBlocks swapped")
