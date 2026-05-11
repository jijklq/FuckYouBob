#!/usr/bin/env python3
"""
Генерирует stub-регистрации для блоков, присутствующих в оригинальном
ModBlocks.java (1.7.10), но отсутствующих в нашей версии (1.12.2).

Вывод — три блока кода для вставки в соответствующие места нашего файла.
"""

import re
import sys

ORIG = r"C:\Users\rad\cc\Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive\src\main\java\com\hbm\blocks\ModBlocks.java"
OURS = r"C:\Users\rad\cc\fuckyoubob\src\main\java\com\hbm\blocks\ModBlocks.java"

# Материалы 1.7.10 → 1.12.2
MATERIAL_MAP = {
    "Material.rock":    "Material.ROCK",
    "Material.iron":    "Material.IRON",
    "Material.sand":    "Material.SAND",
    "Material.glass":   "Material.GLASS",
    "Material.grass":   "Material.GRASS",
    "Material.wood":    "Material.WOOD",
    "Material.ground":  "Material.GROUND",
    "Material.ground":  "Material.GROUND",
    "Material.coral":   "Material.CORAL",
    "Material.leaves":  "Material.LEAVES",
    "Material.cloth":   "Material.CLOTH",
    "Material.plants":  "Material.PLANTS",
    "Material.vine":    "Material.VINE",
    "Material.cactus":  "Material.CACTUS",
    "Material.ice":     "Material.ICE",
    "Material.snow":    "Material.SNOW",
    "Material.craftedSnow": "Material.CRAFTED_SNOW",
    "Material.anvil":   "Material.ANVIL",
    "Material.circuits": "Material.CIRCUITS",
    "Material.gourd":   "Material.GOURD",
    "Material.tnt":     "Material.TNT",
    "Material.sponge":  "Material.SPONGE",
    "Material.web":     "Material.WEB",
    "Material.fire":    "Material.FIRE",
    "Material.lava":    "Material.LAVA",
    "Material.water":   "Material.WATER",
    "Material.air":     "Material.AIR",
    "Material.piston":  "Material.PISTON",
    "Material.barrier": "Material.BARRIER",
    "Material.portal":  "Material.PORTAL",
    "Material.dragonEgg": "Material.DRAGON_EGG",
}

# Creative tabs — оставляем как есть (они те же в нашем коде)
# Блоки с setBlockUnbreakable() получают hardness -1.0F, resistance 6000000.0F

def read_file(path):
    with open(path, encoding="utf-8", errors="replace") as f:
        return f.read()

def get_registered_names(text, is_ours):
    """Извлекает имена блоков, зарегистрированных в данном файле."""
    names = set()
    if is_ours:
        # event.getRegistry().register(block_name) или register(new ItemBlock(block_name)...)
        for m in re.finditer(r'event\.getRegistry\(\)\.register\((?:new ItemBlock\()?(\w+)', text):
            names.add(m.group(1))
    else:
        # GameRegistry.registerBlock(name, ...) или register(name)
        for m in re.finditer(r'GameRegistry\.registerBlock\((\w+)[,)]', text):
            names.add(m.group(1))
        for m in re.finditer(r'\bregister\((\w+)[,)]', text):
            names.add(m.group(1))
    return names

def find_init_line(orig_text, block_name):
    """
    Ищет строку инициализации блока в оригинале.
    Паттерн: block_name = new Something(...).setBlockName(...) ...
    Может быть многострочной — берём всё до ';'.
    """
    # Ищем начало присваивания
    pattern = re.compile(
        r'(?m)^\s*' + re.escape(block_name) + r'\s*=\s*new\s+\w+[^;]*;',
        re.DOTALL
    )
    m = pattern.search(orig_text)
    if m:
        return m.group(0).strip()

    # Иногда поле инициализируется в объявлении (static = new ...)
    pattern2 = re.compile(
        r'public\s+static\s+(?:final\s+)?Block\s+' + re.escape(block_name) + r'\s*=\s*new\s+\w+[^;]*;',
        re.DOTALL
    )
    m2 = pattern2.search(orig_text)
    if m2:
        return m2.group(0).strip()

    return None

def parse_material(init_line):
    """Извлекает материал из new SomeBlock(Material.xxx, ...)."""
    m = re.search(r'new\s+\w+\(([^)]*)\)', init_line)
    if not m:
        return "Material.ROCK"
    args = m.group(1)
    for k, v in MATERIAL_MAP.items():
        if k in args:
            return v
    # Если первый аргумент — переменная или Block.*, по умолчанию ROCK
    return "Material.ROCK"

def parse_float(init_line, setter):
    """Извлекает float-аргумент из .setHardness(x) / .setResistance(x) и т.п."""
    m = re.search(re.escape(setter) + r'\(([-\d./F*]+)\)', init_line)
    if m:
        val = m.group(1).rstrip("Ff")
        # eval простых выражений вида 5F/15F
        try:
            return str(eval(val))
        except:
            return val
    return None

def parse_creative_tab(init_line):
    """Извлекает .setCreativeTab(...)."""
    m = re.search(r'\.setCreativeTab\(([^)]+)\)', init_line)
    if m:
        tab = m.group(1).strip()
        # Маппинг устаревших вкладок
        tab_map = {
            "MainRegistry.blockTab":   "MainRegistry.blockTab",
            "MainRegistry.machineTab": "MainRegistry.machineTab",
            "MainRegistry.nukeTab":    "MainRegistry.nukeTab",
            "MainRegistry.itemTab":    "MainRegistry.itemTab",
            "MainRegistry.weaponTab":  "MainRegistry.weaponTab",
            "CreativeTabs.tabBlock":   "net.minecraft.creativetab.CreativeTabs.BUILDING_BLOCKS",
            "null":                    "null",
        }
        return tab_map.get(tab, tab)
    return None

def is_unbreakable(init_line):
    return "setBlockUnbreakable" in init_line

def build_stub(block_name, init_line):
    """
    Строит строку инициализации в стиле 1.12.2.
    Все кастомные классы заменяются на plain Block(Material.X).
    """
    material  = parse_material(init_line)
    hardness  = parse_float(init_line, ".setHardness")
    resistance = parse_float(init_line, ".setResistance")
    light     = parse_float(init_line, ".setLightLevel")
    tab       = parse_creative_tab(init_line)
    unbreakable = is_unbreakable(init_line)

    parts = [f'new Block({material})']
    parts.append(f'.setUnlocalizedName("{block_name}")')
    parts.append(f'.setRegistryName("hbm", "{block_name}")')
    if tab:
        parts.append(f'.setCreativeTab({tab})')
    if unbreakable:
        parts.append('.setHardness(-1.0F).setResistance(6000000.0F)')
    else:
        if hardness is not None:
            parts.append(f'.setHardness({hardness}F)')
        if resistance is not None:
            parts.append(f'.setResistance({resistance}F)')
    if light is not None:
        parts.append(f'.setLightLevel({light}F)')

    return f'        {block_name} = {"".join(parts)};'

def main():
    orig_text = read_file(ORIG)
    our_text  = read_file(OURS)

    orig_registered = get_registered_names(orig_text, is_ours=False)
    our_registered  = get_registered_names(our_text,  is_ours=True)

    # Блоки, которые есть в оригинале, но нет у нас
    SKIP = {"Block b", "b", "remap", "oc_cable_paintable"}  # oc_cable_paintable — условная регистрация
    missing = sorted(orig_registered - our_registered - SKIP)

    # Фильтруем не-поля (случайные совпадения regex)
    all_fields = set(re.findall(r'public\s+static\s+(?:final\s+)?Block\s+(\w+)', orig_text + our_text))
    missing = [n for n in missing if n in all_fields]

    print(f"// === Найдено {len(missing)} отсутствующих блоков ===\n")

    # --- Секция 1: вставка в initBlocks() ---
    print("// ================================================================")
    print("// 1. ВСТАВИТЬ В initBlocks() — инициализация блоков")
    print("// ================================================================")
    print()

    no_init = []
    for name in missing:
        init_line = find_init_line(orig_text, name)
        if init_line:
            stub = build_stub(name, init_line)
            print(stub)
        else:
            no_init.append(name)

    if no_init:
        print()
        print("        // --- Блоки без найденной инициализации в оригинале (Material.IRON по умолчанию) ---")
        for name in no_init:
            print(f'        {name} = new Block(Material.IRON).setUnlocalizedName("{name}").setRegistryName("hbm", "{name}");')

    # --- Секция 2: registerBlocks event ---
    print()
    print("// ================================================================")
    print("// 2. ВСТАВИТЬ В onRegisterBlocks() — регистрация блоков")
    print("// ================================================================")
    print()
    for name in missing:
        print(f'        event.getRegistry().register({name});')

    # --- Секция 3: registerItems event ---
    print()
    print("// ================================================================")
    print("// 3. ВСТАВИТЬ В onRegisterItems() — регистрация ItemBlock")
    print("// ================================================================")
    print()
    for name in missing:
        print(f'        event.getRegistry().register(new ItemBlock({name}).setRegistryName({name}.getRegistryName()));')

    print()
    print(f"// === Итого: {len(missing)} блоков ===")

if __name__ == "__main__":
    main()
