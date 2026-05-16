#!/usr/bin/env python3
"""
Парсит оригинальный ModBlocks.java (1.7.10) и применяет BlockHazard/BlockHotHazard
к нашему ModBlocks.java (1.12.2).
Сохраняет цепочку .makeBeaconable()/.setDisplayEffect(...) с оригинала.
"""

import re

ORIG_PATH = r"C:\Users\rad\cc\Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive\src\main\java\com\hbm\blocks\ModBlocks.java"
OUR_PATH  = r"C:\Users\rad\cc\fuckyoubob\src\main\java\com\hbm\blocks\ModBlocks.java"

TARGET_CLASSES = {"BlockHazard", "BlockHotHazard"}

MATERIAL_MAP = {
    "Material.rock":   "Material.ROCK",
    "Material.iron":   "Material.IRON",
    "Material.ground": "Material.GROUND",
    "Material.cloth":  "Material.CLOTH",
    "Material.glass":  "Material.GLASS",
    "Material.wood":   "Material.WOOD",
}

IMPORTS_TO_ADD = {
    "BlockHazard":    "import com.hbm.blocks.generic.BlockHazard;",
    "BlockHotHazard": "import com.hbm.blocks.generic.BlockHotHazard;",
}
EFFECT_IMPORT = "import com.hbm.blocks.generic.BlockHazard.ExtDisplayEffect;"

def fix_args(s):
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


def extract_chain(text, start):
    """Extract .makeBeaconable() / .setDisplayEffect(...) chain starting at `start` (after closing paren of ctor)."""
    chain_methods = []
    i = start
    while i < len(text):
        m = re.match(r'\s*\.(makeBeaconable|setDisplayEffect)\s*\(', text[i:])
        if not m:
            break
        method_name = m.group(1)
        paren_start = i + m.end() - 1  # index of '('
        paren_end = find_closing_paren(text[paren_start:])
        if paren_end == -1:
            break
        args = text[paren_start + 1: paren_start + paren_end]
        args = fix_args(args.strip())
        chain_methods.append(f".{method_name}({args})")
        i = paren_start + paren_end + 1
    return "".join(chain_methods), i


def parse_original(orig_path):
    """Returns dict: field_name -> (class_name, ctor_args, extra_chain)"""
    with open(orig_path, "r", encoding="utf-8") as f:
        text = f.read()

    # Match:  field = new BlockHazard(...).makeBeaconable().setDisplayEffect(...)
    pattern = re.compile(
        r'\b(\w+)\s*=\s*new\s+(' + '|'.join(re.escape(c) for c in TARGET_CLASSES) + r')\s*\(',
        re.DOTALL
    )
    mapping = {}
    for m in pattern.finditer(text):
        field = m.group(1)
        cls   = m.group(2)
        # find ctor args
        after_open = m.end()  # right after the '('
        # we need to go back one to include '('
        ctor_start = after_open - 1
        ctor_end = find_closing_paren(text[ctor_start:])
        if ctor_end == -1:
            continue
        args = text[ctor_start + 1: ctor_start + ctor_end].strip()
        args = re.sub(r'\s+', ' ', args)
        args = fix_args(args)

        # chain after closing paren of ctor
        after_ctor = ctor_start + ctor_end + 1
        extra_chain, _ = extract_chain(text, after_ctor)

        mapping[field] = (cls, args, extra_chain)
    return mapping


def apply_to_our_modblocks(our_path, mapping):
    with open(our_path, "r", encoding="utf-8") as f:
        lines = f.readlines()

    counters = {"BlockHazard": 0, "BlockHotHazard": 0}
    changed_classes = set()
    new_lines = []

    # Already-switched classes from phase 4.2 — don't re-touch
    already_switched = {
        "BlockGeneric", "BlockBeaconable", "BlockOutgas", "BlockMulti",
        "BlockDepth", "BlockDirt", "BlockNTMGlass", "BlockGenericStairs", "BlockRedBrick",
        "BlockOre", "BlockGas", "BlockOreOutgas", "BlockFallingTint", "BlockCluster",
        "BlockHazard", "BlockHotHazard",  # don't re-switch if already done
    }

    for line in lines:
        matched = False
        for field, (cls, orig_args, extra_chain) in mapping.items():
            # Only replace if current line has `field = new SomethingElse(`
            pat = re.compile(
                r'^(\s*)' + re.escape(field) +
                r'\s*=\s*new\s+(?!(?:' + '|'.join(re.escape(c) for c in already_switched) + r')\s*\()(\w+)\s*\('
            )
            m = pat.search(line)
            if not m:
                continue
            # Find '=' position, reconstruct from there
            eq_idx = line.index('=', line.index(field))
            suffix = line[eq_idx + 1:]
            # Find end of old ctor call (first '(...)' group)
            open_idx = suffix.index('(')
            close_idx = find_closing_paren(suffix[open_idx:])
            if close_idx == -1:
                break
            after_old_ctor = open_idx + close_idx + 1
            # Rest of line after old ctor (the .setUnlocalizedName()... chain)
            rest = suffix[after_old_ctor:]
            # Strip any old .makeBeaconable()/.setDisplayEffect() that might be there
            # (shouldn't be in our port, but just in case)
            new_ctor = f"new {cls}({orig_args}){extra_chain}"
            new_line = line[:eq_idx + 1] + " " + new_ctor + rest
            new_lines.append(new_line)
            counters[cls] += 1
            changed_classes.add(cls)
            matched = True
            break
        if not matched:
            new_lines.append(line)

    # Add imports
    import_lines_to_add = []
    joined = "".join(new_lines)
    for cls in changed_classes:
        imp = IMPORTS_TO_ADD[cls]
        if imp not in joined:
            import_lines_to_add.append(imp + "\n")
    if changed_classes and EFFECT_IMPORT not in joined:
        import_lines_to_add.append(EFFECT_IMPORT + "\n")

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


def main():
    print(f"Парсинг оригинала: {ORIG_PATH}")
    mapping = parse_original(ORIG_PATH)
    print(f"Найдено полей: {len(mapping)}")
    for cls in TARGET_CLASSES:
        cnt = sum(1 for v in mapping.values() if v[0] == cls)
        fields = [f for f, v in mapping.items() if v[0] == cls]
        effects = [(f, v[2]) for f, v in mapping.items() if v[0] == cls and v[2]]
        print(f"  {cls}: {cnt} — {', '.join(fields[:8])}{'...' if len(fields)>8 else ''}")
        if effects:
            print(f"    chains: " + ", ".join(f"{f}{e}" for f, e in effects))

    print(f"\nПрименение к: {OUR_PATH}")
    counters = apply_to_our_modblocks(OUR_PATH, mapping)

    print("\nРезультат:")
    for cls, n in counters.items():
        print(f"  {cls}: {n} переключено")
    print(f"  ИТОГО: {sum(counters.values())}")


if __name__ == "__main__":
    main()
