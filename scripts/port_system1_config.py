#!/usr/bin/env python3
"""
Портирует System 1 конфиг-файлы (Forge Configuration API) из 1.7.10 в 1.12.2.
Forge Configuration API идентичен в обеих версиях — только точечные фиксы.
"""

import os
import re

ORIG_DIR = r"C:\Users\rad\cc\Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive\src\main\java\com\hbm\config"
DEST_DIR = r"C:\Users\rad\cc\fuckyoubob\src\main\java\com\hbm\config"

# 11 файлов Системы 1 + CommonConfig
SYSTEM1_FILES = [
    "CommonConfig.java",
    "GeneralConfig.java",
    "BombConfig.java",
    "MachineConfig.java",
    "MobConfig.java",
    "PotionConfig.java",
    "RadiationConfig.java",
    "SpaceConfig.java",
    "StructureConfig.java",
    "ToolConfig.java",
    "WeaponConfig.java",
    "WorldConfig.java",
]

def fix_imports(text):
    # FML package rename
    text = text.replace("import cpw.mods.fml.common.Loader;", "import net.minecraftforge.fml.common.Loader;")
    # Убираем импорты нереализованных классов
    for imp in [
        "import com.hbm.inventory.recipes.PrecAssRecipes;",
        "import com.hbm.lib.RefStrings;",
        "import com.hbm.util.Compat;",
        "import com.hbm.dim.BiomeCollisionException;",
        "import com.hbm.handler.radiation.ChunkRadiationHandlerPRISM;",
        "import com.hbm.handler.radiation.ChunkRadiationManager;",
    ]:
        text = text.replace(imp, "")
    return text

def fix_body(text, filename):
    # RefStrings.MODID → "hbm"
    text = text.replace('RefStrings.MODID', '"hbm"')

    # Compat.MOD_EIDS → "endlessids"
    text = text.replace('Compat.MOD_EIDS', '"endlessids"')

    if filename == "GeneralConfig.java":
        # trueExp() ссылается на PrecAssRecipes.INSTANCE.modified → заглушка
        text = text.replace(
            "return enableExpensiveMode && !PrecAssRecipes.INSTANCE.modified;",
            "return false; // TODO: restore when PrecAssRecipes is ported"
        )

    if filename == "RadiationConfig.java":
        # ChunkRadiationManager.proxy = new ChunkRadiationHandlerPRISM(); → комментарий
        text = text.replace(
            "if(enablePRISM) ChunkRadiationManager.proxy = new ChunkRadiationHandlerPRISM();",
            "// TODO: if(enablePRISM) ChunkRadiationManager.proxy = new ChunkRadiationHandlerPRISM();"
        )

    if filename == "SpaceConfig.java":
        # BiomeCollisionException throw → комментарий
        text = re.sub(
            r'throw new BiomeCollisionException\([^;]+\);',
            '// TODO: throw new BiomeCollisionException(...); // BiomeCollisionException not yet ported',
            text
        )

    return text

def clean_blank_lines(text):
    # Убираем двойные пустые строки после удалённых импортов
    return re.sub(r'\n{3,}', '\n\n', text)

def port_file(filename):
    src = os.path.join(ORIG_DIR, filename)
    dst = os.path.join(DEST_DIR, filename)

    with open(src, encoding="utf-8", errors="replace") as f:
        text = f.read()

    text = fix_imports(text)
    text = fix_body(text, filename)
    text = clean_blank_lines(text)

    with open(dst, "w", encoding="utf-8") as f:
        f.write(text)

    lines = text.count('\n')
    print(f"  {filename}: {lines} строк")

def main():
    os.makedirs(DEST_DIR, exist_ok=True)
    print(f"Целевая папка: {DEST_DIR}\n")
    for fname in SYSTEM1_FILES:
        port_file(fname)
    print(f"\nГотово: {len(SYSTEM1_FILES)} файлов.")

if __name__ == "__main__":
    main()
