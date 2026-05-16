#!/usr/bin/env python3
"""
Парсит оригинальный ModBlocks.java (1.7.10) и применяет найденные классы
к нашему ModBlocks.java (1.12.2): заменяет `new Block(Material.X)` на
`new TargetClass(args)` для 9 базовых классов.
"""

import re
import sys
import os

ORIG_PATH = r"C:\Users\rad\cc\Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive\src\main\java\com\hbm\blocks\ModBlocks.java"
OUR_PATH  = r"C:\Users\rad\cc\fuckyoubob\src\main\java\com\hbm\blocks\ModBlocks.java"

TARGET_CLASSES = {
    "BlockGeneric",
    "BlockBeaconable",
    "BlockOutgas",
    "BlockMulti",
    "BlockDepth",
    "BlockDirt",
    "BlockNTMGlass",
    "BlockGenericStairs",
    "BlockRedBrick",
}

# Материалы 1.7.10 → 1.12.2
MATERIAL_MAP = {
    "Material.rock":   "Material.ROCK",
    "Material.iron":   "Material.IRON",
    "Material.ground": "Material.GROUND",
    "Material.wood":   "Material.WOOD",
    "Material.grass":  "Material.GRASS",
    "Material.sand":   "Material.SAND",
    "Material.cloth":  "Material.CLOTH",
    "Material.glass":  "Material.GLASS",
    "Material.tnt":    "Material.TNT",
    "Material.leaves": "Material.LEAVES",
    "Material.plants": "Material.PLANTS",
    "Material.water":  "Material.WATER",
    "Material.lava":   "Material.LAVA",
    "Material.fire":   "Material.FIRE",
    "Material.ice":    "Material.ICE",
    "Material.packedIce": "Material.PACKED_ICE",
    "Material.snow":   "Material.SNOW",
    "Material.craftedSnow": "Material.CRAFTED_SNOW",
    "Material.cactus": "Material.CACTUS",
    "Material.clay":   "Material.CLAY",
    "Material.gourd":  "Material.GOURD",
    "Material.dragonEgg": "Material.DRAGON_EGG",
    "Material.portal": "Material.PORTAL",
    "Material.cake":   "Material.CAKE",
    "Material.web":    "Material.WEB",
    "Material.vine":   "Material.VINE",
    "Material.piston": "Material.PISTON",
    "Material.barrier": "Material.BARRIER",
    "Material.structure": "Material.STRUCTURE",
}

IMPORTS_TO_ADD = {
    "BlockGeneric":       "import com.hbm.blocks.generic.BlockGeneric;",
    "BlockBeaconable":    "import com.hbm.blocks.generic.BlockBeaconable;",
    "BlockOutgas":        "import com.hbm.blocks.generic.BlockOutgas;",
    "BlockMulti":         "import com.hbm.blocks.BlockMulti;",
    "BlockDepth":         "import com.hbm.blocks.generic.BlockDepth;",
    "BlockDirt":          "import com.hbm.blocks.generic.BlockDirt;",
    "BlockNTMGlass":      "import com.hbm.blocks.generic.BlockNTMGlass;",
    "BlockGenericStairs": "import com.hbm.blocks.generic.BlockGenericStairs;",
    "BlockRedBrick":      "import com.hbm.blocks.generic.BlockRedBrick;",
}

def fix_args(args_str):
    result = args_str
    for old, new in MATERIAL_MAP.items():
        result = result.replace(old, new)
    return result


def parse_original(orig_path):
    """Returns dict: field_name -> (class_name, ctor_args_str)"""
    with open(orig_path, "r", encoding="utf-8") as f:
        text = f.read()

    pattern = re.compile(
        r'\b(\w+)\s*=\s*new\s+(' + '|'.join(re.escape(c) for c in TARGET_CLASSES) + r')\s*\(([^;]*?)\)\s*[;.]',
        re.DOTALL
    )
    mapping = {}
    for m in pattern.finditer(text):
        field = m.group(1)
        cls   = m.group(2)
        args  = m.group(3).strip()
        # Убираем многострочные пробелы
        args  = re.sub(r'\s+', ' ', args)
        mapping[field] = (cls, args)
    return mapping


def apply_to_our_modblocks(our_path, mapping):
    with open(our_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    counters = {cls: 0 for cls in TARGET_CLASSES}
    new_lines = []
    changed_classes = set()

    for line in lines:
        matched = False
        for field, (cls, orig_args) in mapping.items():
            # Ищем строку вида:  field_name = new Block(Material.X)...
            # Должна содержать field_name = new <что-то>( — но не уже нужный класс
            # Используем lookahead чтобы не трогать уже переключённые
            pat = re.compile(
                r'^(\s*)' + re.escape(field) + r'\s*=\s*new\s+(?!' + re.escape(cls) + r'\s*\()(\w+)\s*\([^;]*\)',
            )
            m = pat.search(line)
            if m:
                indent = m.group(1)
                # Берём всё что идёт после '=' до первой ';' включительно
                # (там может быть .setUnlocalizedName... и т.д.)
                eq_pos = line.index('=', line.index(field))
                suffix_start = line.index('=', line.index(field)) + 1
                suffix = line[suffix_start:]
                # Находим конец new OldClass(...)
                paren_end = find_closing_paren(suffix)
                if paren_end == -1:
                    break
                # Сохраняем цепочку после закрывающей скобки
                chain = suffix[paren_end + 1:]
                new_args = fix_args(orig_args)
                new_ctor = f"new {cls}({new_args})"
                new_line = line[:suffix_start] + " " + new_ctor + chain
                new_lines.append(new_line)
                counters[cls] += 1
                changed_classes.add(cls)
                matched = True
                break
        if not matched:
            new_lines.append(line)

    # Добавить импорты если нужно
    # Ищем блок импортов — вставляем после последнего import
    import_lines_to_add = [IMPORTS_TO_ADD[cls] + "\n" for cls in changed_classes
                           if IMPORTS_TO_ADD[cls] not in "".join(new_lines)]
    if import_lines_to_add:
        last_import_idx = 0
        for i, line in enumerate(new_lines):
            if line.strip().startswith("import "):
                last_import_idx = i
        for il in sorted(import_lines_to_add):
            new_lines.insert(last_import_idx + 1, il)
            last_import_idx += 1

    with open(our_path, "w", encoding="utf-8") as f:
        f.writelines(new_lines)

    return counters


def find_closing_paren(s):
    """Find index of closing paren for the first '(' in s, handling nesting."""
    depth = 0
    for i, c in enumerate(s):
        if c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return i
    return -1


def main():
    print(f"Парсинг оригинала: {ORIG_PATH}")
    mapping = parse_original(ORIG_PATH)
    print(f"Найдено полей в оригинале: {len(mapping)}")
    for cls in TARGET_CLASSES:
        cnt = sum(1 for v in mapping.values() if v[0] == cls)
        if cnt:
            print(f"  {cls}: {cnt} полей")

    print(f"\nПрименение к: {OUR_PATH}")
    counters = apply_to_our_modblocks(OUR_PATH, mapping)

    print("\nРезультат переключения:")
    total = 0
    for cls in TARGET_CLASSES:
        n = counters[cls]
        if n:
            print(f"  {cls}: {n} полей переключено")
            total += n
    print(f"  ИТОГО: {total} полей")


if __name__ == "__main__":
    main()
