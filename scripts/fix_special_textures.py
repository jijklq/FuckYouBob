"""
Fix remaining model JSONs after fix_block_texture_names.py:
1. RBMK - textures in rbmk/ subdirectory
2. Stairs/slabs - use parent block texture
3. Vanilla aliases - reference minecraft:blocks/...
4. Crops - reference the leaf/plant texture by crop suffix
"""

import re
import os
import json

MODELS_BLOCK   = "src/main/resources/assets/hbm/models/block"
TEX_BLOCKS_DIR = "C:/Users/rad/cc/Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive/src/main/resources/assets/hbm/textures/blocks"

# Manual overrides: block_id -> texture path (relative to hbm:blocks/ or full "minecraft:blocks/X")
OVERRIDES = {
    # Vanilla aliases
    "ntm_dirt":                  "minecraft:blocks/dirt",
    "skeleton_holder":           "minecraft:blocks/soul_sand",
    "capacitor_gold":            "minecraft:blocks/gold_block",
    "machine_ashpit":            "minecraft:blocks/stonebrick",
    "machine_industrial_generator": "minecraft:blocks/gold_block",

    # Crops: crop_X uses X texture
    "crop_coffee":    "hbm:blocks/coffee",
    "crop_mint":      "hbm:blocks/mint",
    "crop_paraffin":  "hbm:blocks/paraffin",
    "crop_strawberry":"hbm:blocks/strawberry",
    "crop_tea":       "hbm:blocks/tea",
}

# Stairs: strip suffixes to find parent texture
STAIR_SUFFIXES = ["_stairs", "_slab", "_double_slab"]
# Explicit stair parent mappings where suffix-strip doesn't work
STAIR_PARENTS = {
    "brick_asbestos_stairs":        "brick_asbestos",
    "brick_compound_stairs":        "brick_compound",
    "brick_concrete_broken_stairs": "brick_concrete_broken",
    "brick_concrete_cracked_stairs":"brick_concrete_cracked",
    "brick_concrete_mossy_stairs":  "brick_concrete_mossy",
    "brick_concrete_stairs":        "brick_concrete",
    "brick_ducrete_stairs":         "brick_ducrete",
    "brick_fire_stairs":            "brick_fire",
    "brick_light_stairs":           "brick_light",
    "brick_obsidian_stairs":        "brick_obsidian",
    "brick_slab":                   "brick_concrete",
    "brick_double_slab":            "brick_concrete",
    "asphalt_stairs":               "asphalt",
    "asphalt_light":                "asphalt_light",  # has own texture, keep
    "concrete_asbestos_stairs":     "concrete_asbestos",
    "concrete_brick_slab":          "concrete_brick",
    "concrete_brick_double_slab":   "concrete_brick",
    "concrete_double_slab":         "concrete_smooth",
    "concrete_slab":                "concrete_smooth",
    "concrete_smooth_stairs":       "concrete_smooth",
    "concrete_stairs":              "concrete_smooth",
    "ducrete_smooth_stairs":        "ducrete_smooth",
    "ducrete_stairs":               "ducrete",
    "lightstone_bricks_stairs":     "lightstone_bricks",
    "lightstone_tile_stairs":       "lightstone_tile",
    "reinforced_brick_stairs":      "reinforced_brick",
    "reinforced_stone_stairs":      "reinforced_stone",
    "stones_slab":                  "stones",
    "stones_double_slab":           "stones",
}

def tex_exists(name):
    """Check if texture exists (hbm namespace only)."""
    return os.path.exists(os.path.join(TEX_BLOCKS_DIR, name + ".png"))

def tex_exists_subdir(path):
    """Check texture with subdirectory, e.g. rbmk/rbmk_absorber."""
    return os.path.exists(os.path.join(TEX_BLOCKS_DIR, path + ".png"))

def main():
    updated = skipped = 0

    for fname in sorted(os.listdir(MODELS_BLOCK)):
        if not fname.endswith(".json"):
            continue
        bid = fname[:-5]
        path = os.path.join(MODELS_BLOCK, fname)

        with open(path) as f:
            try:
                data = json.load(f)
            except json.JSONDecodeError:
                continue

        if data.get("parent") != "block/cube_all":
            continue

        current_tex = data.get("textures", {}).get("all", "")
        tex_name = current_tex.replace("hbm:blocks/", "")

        # Already has a valid texture?
        if tex_exists(tex_name) or tex_exists_subdir(tex_name):
            skipped += 1
            continue

        new_tex = None

        # 1. Manual overrides
        if bid in OVERRIDES:
            new_tex = OVERRIDES[bid]

        # 2. Explicit stair parents
        elif bid in STAIR_PARENTS:
            parent = STAIR_PARENTS[bid]
            if tex_exists(parent):
                new_tex = f"hbm:blocks/{parent}"

        # 3. RBMK - subdirectory (texture path already has rbmk/ prefix from mapping)
        elif tex_name.startswith("rbmk/") or bid.startswith("rbmk_"):
            subpath = tex_name  # e.g. rbmk/rbmk_absorber
            if tex_exists_subdir(subpath):
                new_tex = f"hbm:blocks/{subpath}"
            else:
                # Try rbmk/ prefix on the bid
                candidate = "rbmk/" + bid
                if tex_exists_subdir(candidate):
                    new_tex = f"hbm:blocks/{candidate}"

        if new_tex is None:
            continue  # leave as-is, will be pink until texture/TE is ported

        data["textures"]["all"] = new_tex
        with open(path, "w") as f:
            json.dump(data, f, indent=2)
        updated += 1
        print(f"  {bid} -> {new_tex}")

    print(f"\nUpdated: {updated}, Already OK: {skipped}")

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    main()
