import re
from pathlib import Path

p = Path("src/main/java/com/hbm/blocks/ModBlocks.java")
src = p.read_text(encoding="utf-8")

imports_to_add = [
    "import com.hbm.blocks.bomb.BlockTaint;",
]
for imp in imports_to_add:
    if imp not in src:
        src = re.sub(r"(\nimport [^\n]+;\n)(?!\nimport)", r"\1" + imp + "\n", src, count=1)

old_line = 'taint = new Block(Material.IRON).setUnlocalizedName("taint").setRegistryName("hbm", "taint").setCreativeTab(null).setHardness(15.0F).setResistance(10.0F);'
new_line = 'taint = new BlockTaint(Material.IRON).setUnlocalizedName("taint").setRegistryName("hbm", "taint").setCreativeTab(null).setHardness(15.0F).setResistance(10.0F);'

n = src.count(old_line)
if n != 1:
    raise SystemExit(f"FAILED: taint match count={n}")

src = src.replace(old_line, new_line)
p.write_text(src, encoding="utf-8")
print("OK: 1 field swapped (taint)")
