#!/usr/bin/env python3
"""Replace gas_ blocks from new Block(Material.AIR) to new BlockGas(Material.AIR)."""
import re, os

MODBLOCKS = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'java', 'com', 'hbm', 'blocks', 'ModBlocks.java')

GAS_FIELDS = {
    'gas_radon', 'gas_radon_dense', 'gas_radon_tomb',
    'gas_meltdown', 'gas_monoxide', 'gas_asbestos',
    'gas_coal', 'gas_flammable', 'gas_explosive',
}

PATTERN = re.compile(r'^(\s+)(gas_\w+)\s*=\s*new Block\((Material\.\w+)\)(.*)')
IMPORT_LINE = 'import com.hbm.blocks.generic.BlockGas;\n'

with open(MODBLOCKS, 'r', encoding='utf-8') as f:
    lines = f.readlines()

changed = 0
result = []
import_added = False

for line in lines:
    if not import_added and 'import com.hbm.blocks.generic.BlockOre;' in line:
        result.append(IMPORT_LINE)
        import_added = True

    m = PATTERN.match(line)
    if m:
        indent, field, material, rest = m.groups()
        if field in GAS_FIELDS:
            result.append(f'{indent}{field} = new BlockGas({material}){rest}\n')
            changed += 1
            continue

    result.append(line)

with open(MODBLOCKS, 'w', encoding='utf-8') as f:
    f.writelines(result)

print(f'Changed: {changed} gas blocks')
