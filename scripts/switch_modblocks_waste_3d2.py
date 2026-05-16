import re
from pathlib import Path

p = Path("src/main/java/com/hbm/blocks/ModBlocks.java")
src = p.read_text(encoding="utf-8")

imports_to_add = [
    "import com.hbm.blocks.generic.WasteEarth;",
    "import net.minecraft.block.SoundType;",
]
for imp in imports_to_add:
    if imp not in src:
        src = re.sub(r"(\nimport [^\n]+;\n)(?!\nimport)", r"\1" + imp + "\n", src, count=1)

full_line_swaps = [
    (
        'waste_earth = new Block(Material.GROUND).setUnlocalizedName("waste_earth").setRegistryName("hbm", "waste_earth").setCreativeTab(MainRegistry.blockTab).setHardness(0.6F);',
        'waste_earth = new WasteEarth(Material.GROUND, true, SoundType.PLANT).setUnlocalizedName("waste_earth").setRegistryName("hbm", "waste_earth").setCreativeTab(MainRegistry.blockTab).setHardness(0.6F);'
    ),
    (
        'waste_mycelium = new Block(Material.GROUND).setUnlocalizedName("waste_mycelium").setRegistryName("hbm", "waste_mycelium").setCreativeTab(MainRegistry.blockTab).setHardness(0.6F).setLightLevel(1F);',
        'waste_mycelium = new WasteEarth(Material.GROUND, true, SoundType.PLANT).setUnlocalizedName("waste_mycelium").setRegistryName("hbm", "waste_mycelium").setCreativeTab(MainRegistry.blockTab).setHardness(0.6F).setLightLevel(1F);'
    ),
    (
        'frozen_grass = new Block(Material.GROUND).setUnlocalizedName("frozen_grass").setRegistryName("hbm", "frozen_grass").setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);',
        'frozen_grass = new WasteEarth(Material.GROUND, false, SoundType.GLASS).setUnlocalizedName("frozen_grass").setRegistryName("hbm", "frozen_grass").setCreativeTab(MainRegistry.blockTab).setHardness(0.5F).setResistance(2.5F);'
    ),
    (
        'burning_earth = new Block(Material.GROUND).setUnlocalizedName("burning_earth").setRegistryName("hbm", "burning_earth").setCreativeTab(MainRegistry.blockTab).setHardness(0.6F);',
        'burning_earth = new WasteEarth(Material.GROUND, true, SoundType.PLANT).setUnlocalizedName("burning_earth").setRegistryName("hbm", "burning_earth").setCreativeTab(MainRegistry.blockTab).setHardness(0.6F);'
    ),
]

for old, new in full_line_swaps:
    n = src.count(old)
    if n != 1:
        raise SystemExit(f"FAILED: expected 1 match for old line, got {n}: {old[:80]}...")
    src = src.replace(old, new)

p.write_text(src, encoding="utf-8")
print("OK: 4 fields swapped (with setSoundType via constructor)")
