#!/usr/bin/env python3
"""
Парсит оригинальный ModBlocks.java (1.7.10) и применяет BlockFallout/BlockHazardFalling
к нашему ModBlocks.java (1.12.2).
Переключает 3 поля: fallout, salted_fallout, block_fallout.
"""

import re

ORIG_PATH = r"C:\Users\rad\cc\Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive\src\main\java\com\hbm\blocks\ModBlocks.java"
OUR_PATH  = r"C:\Users\rad\cc\fuckyoubob\src\main\java\com\hbm\blocks\ModBlocks.java"

TARGET_FIELDS = {"fallout", "salted_fallout", "block_fallout"}

# BlockFallout принимает Material, BlockHazardFalling может без аргументов
TARGET_CLASSES = {"BlockFallout", "BlockHazardFalling"}

MATERIAL_MAP = {
    "Material.sand":  "Material.SAND",
    "Material.snow":  "Material.SNOW",
    "Material.rock":  "Material.ROCK",
    "Material.iron":  "Material.IRON",
    "Material.ground":"Material.GROUND",
    "Material.cloth": "Material.CLOTH",
    "Material.glass": "Material.GLASS",
    "Material.wood":  "Material.WOOD",
    "Material.water": "Material.WATER",
}

IMPORTS_TO_ADD = {
    "BlockFallout":       "import com.hbm.blocks.generic.BlockFallout;",
    "BlockHazardFalling": "import com.hbm.blocks.generic.BlockHazardFalling;",
}


def fix_material(s):
    for old, new in MATERIAL_MAP.items():
        s = s.replace(old, new)
    return s


def find_closing_paren(s):
    depth = 0
    for i, c in enumerate(s):
        if c == '(':
            depth += 1
        elif c == ')':
            depth -= 1
            if depth == 0:
                return i
    return -1


def parse_original(orig_path):
    """Returns dict: field_name -> (class_name, ctor_args_fixed)"""
    with open(orig_path, "r", encoding="utf-8") as f:
        text = f.read()

    pattern = re.compile(
        r'\b(' + '|'.join(re.escape(f) for f in TARGET_FIELDS) + r')\s*=\s*new\s+'
        r'(' + '|'.join(re.escape(c) for c in TARGET_CLASSES) + r')\s*\(',
        re.DOTALL
    )
    mapping = {}
    for m in pattern.finditer(text):
        field = m.group(1)
        cls   = m.group(2)
        ctor_open = m.end() - 1  # index of '('
        ctor_end  = find_closing_paren(text[ctor_open:])
        if ctor_end == -1:
            continue
        args = text[ctor_open + 1: ctor_open + ctor_end].strip()
        args = re.sub(r'\s+', ' ', args)
        args = fix_material(args)
        mapping[field] = (cls, args)
    return mapping


def apply_to_our_modblocks(our_path, mapping):
    with open(our_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    counters = {cls: 0 for cls in TARGET_CLASSES}
    changed_classes = set()
    new_lines = []

    for line in lines:
        matched = False
        for field, (cls, orig_args) in mapping.items():
            # Only replace if field = new <something other than target class>(
            pat = re.compile(
                r'^(\s*)' + re.escape(field) +
                r'\s*=\s*new\s+(?!' + '|'.join(re.escape(c) for c in TARGET_CLASSES) + r'\s*\()(\w+)\s*\('
            )
            if not pat.search(line):
                continue
            # Rebuild: keep everything up to and including '=', replace ctor, keep rest of chain
            eq_match = re.search(re.escape(field) + r'\s*=', line)
            if not eq_match:
                continue
            eq_end = line.index('=', eq_match.start()) + 1
            suffix = line[eq_end:]
            open_idx = suffix.index('(')
            close_idx = find_closing_paren(suffix[open_idx:])
            if close_idx == -1:
                break
            after_old_ctor = open_idx + close_idx + 1
            rest = suffix[after_old_ctor:]
            # Strip setBlockTextureName / setBlockName from rest
            rest = re.sub(r'\s*\.setBlockTextureName\s*\([^)]*\)', '', rest)
            rest = re.sub(r'\s*\.setBlockName\s*\([^)]*\)', '', rest)
            new_ctor = f"new {cls}({orig_args})"
            new_line = line[:eq_end] + " " + new_ctor + rest
            new_lines.append(new_line)
            counters[cls] += 1
            changed_classes.add(cls)
            matched = True
            break
        if not matched:
            new_lines.append(line)

    # Add missing imports
    joined = "".join(new_lines)
    import_lines_to_add = []
    for cls in changed_classes:
        imp = IMPORTS_TO_ADD[cls]
        if imp not in joined:
            import_lines_to_add.append(imp + "\n")

    if import_lines_to_add:
        last_import_idx = 0
        for i, ln in enumerate(new_lines):
            if ln.strip().startswith("import "):
                last_import_idx = i
        for il in sorted(import_lines_to_add):
            new_lines.insert(last_import_idx + 1, il)
            last_import_idx += 1

    with open(our_path, "w", encoding="utf-8") as f:
        f.writelines(new_lines)

    return counters


def main():
    print(f"Парсинг оригинала: {ORIG_PATH}")
    mapping = parse_original(ORIG_PATH)
    print(f"Найдено полей: {len(mapping)}")
    for field, (cls, args) in mapping.items():
        print(f"  {field} -> new {cls}({args})")

    if not mapping:
        print("ОШИБКА: ничего не найдено в оригинале. Проверь пути и имена классов.")
        return

    print(f"\nПрименение к: {OUR_PATH}")
    counters = apply_to_our_modblocks(OUR_PATH, mapping)

    total = sum(counters.values())
    print("\nРезультат:")
    for cls, n in counters.items():
        if n:
            print(f"  {cls}: {n} переключено")
    print(f"  ИТОГО: {total} переключено")


if __name__ == "__main__":
    main()
