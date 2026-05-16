import re
from pathlib import Path

p = Path("src/main/java/com/hbm/blocks/ModBlocks.java")
src = p.read_text(encoding="utf-8")

imports_to_add = [
    "import com.hbm.blocks.generic.BlockSellafield;",
    "import com.hbm.blocks.generic.BlockSellafieldOre;",
    "import com.hbm.blocks.generic.BlockSellafieldSlaked;",
    "import com.hbm.items.block.ItemBlockNamedMeta;",
]
for imp in imports_to_add:
    if imp not in src:
        src = re.sub(r"(\nimport [^\n]+;\n)(?!\nimport)", r"\1" + imp + "\n", src, count=1)

field_swaps = [
    ("sellafield_slaked",              "new Block(Material.ROCK)",                            "new BlockSellafieldSlaked(Material.ROCK)"),
    ("sellafield_bedrock",             "new Block(Material.ROCK)",                            "new BlockSellafieldSlaked(Material.ROCK)"),
    ("sellafield",                     "new Block(Material.ROCK)",                            "new BlockSellafield(Material.ROCK)"),
    ("ore_sellafield_diamond",         "new BlockOre(Material.ROCK)",                         "new BlockSellafieldOre(Material.ROCK)"),
    ("ore_sellafield_emerald",         "new BlockOre(Material.ROCK)",                         "new BlockSellafieldOre(Material.ROCK)"),
    ("ore_sellafield_radgem",          "new BlockOre(Material.ROCK)",                         "new BlockSellafieldOre(Material.ROCK)"),
    ("ore_sellafield_schrabidium",     "new BlockOre(Material.ROCK)",                         "new BlockSellafieldOre(Material.ROCK)"),
    ("ore_sellafield_uranium_scorched","new BlockOreOutgas(Material.ROCK, true, 5, true)",    "new BlockSellafieldOre(Material.ROCK)"),
]
for field, old_ctor, new_ctor in field_swaps:
    pattern = rf"({re.escape(field)}\s*=\s*){re.escape(old_ctor)}"
    new_text, n = re.subn(pattern, r"\1" + new_ctor, src)
    if n != 1:
        raise SystemExit(f"FAILED: {field} matched {n} times (expected 1)")
    src = new_text

old_ib = "new ItemBlock(sellafield).setRegistryName(sellafield.getRegistryName())"
new_ib = "new ItemBlockNamedMeta(sellafield).setRegistryName(sellafield.getRegistryName())"
if src.count(old_ib) != 1:
    raise SystemExit(f"FAILED: sellafield ItemBlock match count={src.count(old_ib)}")
src = src.replace(old_ib, new_ib)

p.write_text(src, encoding="utf-8")
print("OK: 8 fields + 1 ItemBlock swapped")
