#!/usr/bin/env python3
"""
Phase 1.5: Composite ore overlay textures onto stone.png background.

Ore textures from 1.7.10 are transparent overlays (no stone background).
In 1.12.2 the multi-pass renderer is gone, so we pre-compose them.

Run from repo root:
    python scripts/composite_ore_textures.py
"""

import os
from PIL import Image

TEXTURES = os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources', 'assets', 'hbm', 'textures', 'blocks')
STONE = os.path.join(TEXTURES, 'stone.png')

# Don't composite — these need a different base or are deferred
EXCLUDE_PREFIXES = (
    'ore_nether_',    # base = netherrack
    'ore_gneiss_',    # base = gneiss
    'ore_meteor_',    # deferred (BlockEnumMulti)
    'ore_basalt_',    # deferred (BlockEnumMulti)
    'ore_bedrock_',   # deferred (TileEntity)
    'ore_sellafield_',# base = sellafield stone
    'ore_depth_',     # base = depth stone
    'ore_tektite_',   # base = tektite sand
)

# Threshold: if >50% of pixels are non-transparent, it's already a full texture
FULL_THRESHOLD = 0.5

stone_base = Image.open(STONE).convert('RGBA')

ore_files = [f for f in os.listdir(TEXTURES) if f.startswith('ore_') and f.endswith('.png')]

composited = []
skipped_full = []
skipped_exclude = []

for fname in sorted(ore_files):
    # Check exclude prefixes
    excluded = any(fname.startswith(p) for p in EXCLUDE_PREFIXES)
    if excluded:
        skipped_exclude.append(fname)
        continue

    path = os.path.join(TEXTURES, fname)
    img = Image.open(path).convert('RGBA')

    # Check if already a full texture (>50% opaque pixels)
    pixels = list(img.getdata())
    total = len(pixels)
    opaque = sum(1 for p in pixels if p[3] > 10)
    if opaque / total > FULL_THRESHOLD:
        skipped_full.append(fname)
        continue

    # Composite: stone base + ore overlay
    if img.size != stone_base.size:
        base = stone_base.resize(img.size, Image.NEAREST)
    else:
        base = stone_base.copy()

    base.paste(img, (0, 0), img)
    base.save(path)
    composited.append(fname)

print(f'Composited (overlay + stone): {len(composited)}')
print(f'Skipped (already full):       {len(skipped_full)}')
print(f'Skipped (excluded prefix):    {len(skipped_exclude)}')
print()
if skipped_exclude:
    print('Excluded (need different base or deferred):')
    for f in skipped_exclude:
        print(f'  {f}')
