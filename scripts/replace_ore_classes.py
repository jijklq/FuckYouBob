#!/usr/bin/env python3
"""
Phase 1.4: Switch ore_ fields in ModBlocks.java from new Block() stubs
to new BlockOre() or new BlockOreOutgas().

Run from the repo root:
    python scripts/replace_ore_classes.py
"""

import re
import os

MODBLOCKS = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'java', 'com', 'hbm', 'blocks', 'ModBlocks.java')

# Fields that get BlockOreOutgas(mat, true, 5, true)
# These were meta-variants of the three original BlockOreOutgas blocks in 1.7.10:
#   ore_uranium / ore_plutonium / ore_asbestos
OUTGAS = {
    'ore_uranium', 'ore_uranium_scorched',
    'ore_plutonium', 'ore_nether_plutonium',
    'ore_nether_uranium', 'ore_nether_uranium_scorched',
    'ore_gneiss_uranium', 'ore_gneiss_uranium_scorched',
    'ore_asbestos', 'ore_gneiss_asbestos',
    'ore_sellafield_uranium_scorched',
}

# Fields that stay as new Block() stubs (dependencies not yet ported)
EXCLUDE = {
    # BlockOreFluid — depends on Fluids system (not yet ported)
    'ore_oil', 'ore_gas', 'ore_brine', 'ore_tekto',
    'ore_oil_empty', 'ore_gas_empty', 'ore_brine_empty', 'ore_tekto_empty',
    # BlockOreBasalt / BlockMeteorOre — depend on BlockEnumMulti (not yet ported)
    'ore_basalt', 'ore_meteor',
    # BlockBedrockOreTE — TileEntity stage
    'ore_bedrock', 'ore_bedrock_oil',
    # Fluid ore in gneiss layer
    'ore_gneiss_gas',
}

# Matches an ore_ init line: "        ore_foo = new Block(Material.X)..."
PATTERN = re.compile(r'^(\s+)(ore_\w+)\s*=\s*new Block\((Material\.\w+)\)(.*)')

IMPORT_ORE       = 'import com.hbm.blocks.generic.BlockOre;\n'
IMPORT_ORE_OUTGAS = 'import com.hbm.blocks.generic.BlockOreOutgas;\n'

with open(MODBLOCKS, 'r', encoding='utf-8') as f:
    lines = f.readlines()

changed_ore = 0
changed_outgas = 0
skipped = 0
result = []
imports_added = False

for line in lines:
    # Inject imports after "import net.minecraft.block.Block;"
    if not imports_added and 'import net.minecraft.block.Block;' in line:
        result.append(line)
        result.append(IMPORT_ORE)
        result.append(IMPORT_ORE_OUTGAS)
        imports_added = True
        continue

    m = PATTERN.match(line)
    if m:
        indent, field, material, rest = m.groups()
        if field in EXCLUDE:
            skipped += 1
            result.append(line)
        elif field in OUTGAS:
            result.append(f'{indent}{field} = new BlockOreOutgas({material}, true, 5, true){rest}\n')
            changed_outgas += 1
        else:
            result.append(f'{indent}{field} = new BlockOre({material}){rest}\n')
            changed_ore += 1
    else:
        result.append(line)

with open(MODBLOCKS, 'w', encoding='utf-8') as f:
    f.writelines(result)

print(f'BlockOre:      {changed_ore} fields')
print(f'BlockOreOutgas:{changed_outgas} fields')
print(f'Skipped (stub):{skipped} fields')
print(f'Total changed: {changed_ore + changed_outgas}')
if not imports_added:
    print('WARNING: imports NOT injected — check the import anchor line in ModBlocks.java')
