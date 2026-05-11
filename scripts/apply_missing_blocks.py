#!/usr/bin/env python3
"""
Вставляет сгенерированные блоки в три места нашего ModBlocks.java:
1. Инициализации — в конец initBlocks(), перед if(Loader.isModLoaded...)
2. Регистрации блоков — в конец registerBlocks(), перед if(oc_cable_paintable != null) { event.getRegistry().register(oc_cable_paintable)
3. Регистрации ItemBlock — в конец registerItemBlocks(), перед if(oc_cable_paintable != null) { event.getRegistry().register(new ItemBlock(oc_cable_paintable)
"""

import re
import sys
import os

sys.path.insert(0, os.path.dirname(__file__))
import gen_missing_blocks as gen

OURS = r"C:\Users\rad\cc\fuckyoubob\src\main\java\com\hbm\blocks\ModBlocks.java"

def collect_sections(missing, orig_text):
    init_lines = []
    reg_block_lines = []
    reg_item_lines = []

    no_init = []
    for name in missing:
        init_line = gen.find_init_line(orig_text, name)
        if init_line:
            init_lines.append(gen.build_stub(name, init_line))
        else:
            no_init.append(name)

    for name in no_init:
        init_lines.append(
            f'        {name} = new Block(Material.IRON)'
            f'.setUnlocalizedName("{name}").setRegistryName("hbm", "{name}");'
        )

    for name in missing:
        reg_block_lines.append(f'        event.getRegistry().register({name});')
        reg_item_lines.append(
            f'        event.getRegistry().register('
            f'new ItemBlock({name}).setRegistryName({name}.getRegistryName()));'
        )

    return init_lines, reg_block_lines, reg_item_lines

def insert_before(text, anchor, new_lines):
    """Вставляет new_lines перед первым вхождением anchor."""
    idx = text.find(anchor)
    if idx == -1:
        raise ValueError(f"Anchor not found: {anchor!r}")
    block = "\n".join(new_lines) + "\n"
    return text[:idx] + block + text[idx:]

def main():
    orig_text = gen.read_file(gen.ORIG)
    our_text  = gen.read_file(gen.OURS)

    orig_registered = gen.get_registered_names(orig_text, is_ours=False)
    our_registered  = gen.get_registered_names(our_text,  is_ours=True)

    SKIP = {"Block b", "b", "remap", "oc_cable_paintable"}
    all_fields = set(re.findall(r'public\s+static\s+(?:final\s+)?Block\s+(\w+)', orig_text + our_text))
    missing = sorted(orig_registered - our_registered - SKIP)
    missing = [n for n in missing if n in all_fields]

    print(f"Блоков к добавлению: {len(missing)}")

    init_lines, reg_block_lines, reg_item_lines = collect_sections(missing, orig_text)

    text = our_text

    # 1. Вставка инициализаций в initBlocks() — перед if(Loader.isModLoaded("OpenComputers"))
    anchor_init = '        if(Loader.isModLoaded("OpenComputers"))'
    text = insert_before(text, anchor_init, init_lines)
    print(f"  initBlocks(): вставлено {len(init_lines)} строк")

    # 2. Вставка регистрации блоков — перед: if(oc_cable_paintable != null) {\n            event.getRegistry().register(oc_cable_paintable);
    anchor_reg = '        if(oc_cable_paintable != null) {\n            event.getRegistry().register(oc_cable_paintable);\n        }\n    }\n\n    @SubscribeEvent\n    public static void registerItemBlocks'
    text = insert_before(text, anchor_reg, reg_block_lines)
    print(f"  registerBlocks(): вставлено {len(reg_block_lines)} строк")

    # 3. Вставка ItemBlock — перед: if(oc_cable_paintable != null) {\n            event.getRegistry().register(new ItemBlock(oc_cable_paintable)
    anchor_item = '        if(oc_cable_paintable != null) {\n            event.getRegistry().register(new ItemBlock(oc_cable_paintable)'
    text = insert_before(text, anchor_item, reg_item_lines)
    print(f"  registerItemBlocks(): вставлено {len(reg_item_lines)} строк")

    with open(OURS, "w", encoding="utf-8") as f:
        f.write(text)
    print("Файл записан.")

if __name__ == "__main__":
    main()
