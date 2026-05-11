"""
Parse original 1.7.10 ModBlocks.java to extract blockName -> textureName mapping.
Update models/block/*.json to reference the correct texture.
Blocks where setBlockTextureName was never called use the block name as texture (default).
"""

import re
import os
import json

ORIG_MODBLOCKS = "C:/Users/rad/cc/Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive/src/main/java/com/hbm/blocks/ModBlocks.java"
MODELS_BLOCK   = "src/main/resources/assets/hbm/models/block"
TEX_BLOCKS_DIR = "C:/Users/rad/cc/Hbm-s-Nuclear-Tech-GIT-space-travel-twopointfive/src/main/resources/assets/hbm/textures/blocks"

def main():
    with open(ORIG_MODBLOCKS, encoding="utf-8") as f:
        src = f.read()

    # Build map: blockName -> textureName
    # Each statement may span one long line:
    # .setBlockName("X")...setBlockTextureName(MODID + ":Y")
    # or setBlockTextureName("hbm:Y")
    # Strategy: find all setBlockTextureName calls with their context
    # First pass: collect all (blockName, textureName) pairs from same statement
    mapping = {}  # blockName -> textureName (without "hbm:" prefix)

    # Match lines that have both setBlockName and setBlockTextureName
    # Pattern: setBlockName("X") ... setBlockTextureName(... ":Y")
    pattern_both = re.compile(
        r'setBlockName\("([^"]+)"\)'   # blockName
        r'.*?'
        r'setBlockTextureName\([^"]*"(?:[^:]+:)?([^"]+)"\)',  # textureName
        re.DOTALL
    )

    # Split source into statements (semicolon-separated, roughly)
    # Use a simpler approach: find each setBlockTextureName and look backwards
    # for the nearest setBlockName on the same "chain"

    # Process line by line (most chains are on one logical line, possibly wrapped)
    # Join continuation lines (lines ending without semicolon)
    # Actually: join the whole file into logical statements split by ';'
    statements = src.split(';')

    for stmt in statements:
        m = pattern_both.search(stmt)
        if m:
            block_name = m.group(1)
            tex_name   = m.group(2).lstrip(":")  # strip leading colon from ":texture_name"
            mapping[block_name] = tex_name

    print(f"Found {len(mapping)} explicit blockName -> textureName mappings")

    # Now update model JSONs
    updated = same = missing_tex = 0
    for fname in os.listdir(MODELS_BLOCK):
        if not fname.endswith(".json"):
            continue
        block_id = fname[:-5]  # strip .json
        path = os.path.join(MODELS_BLOCK, fname)

        with open(path) as f:
            try:
                data = json.load(f)
            except json.JSONDecodeError:
                continue

        if data.get("parent") != "block/cube_all":
            continue  # already customized, skip

        current_tex = data.get("textures", {}).get("all", "")
        # current_tex is like "hbm:blocks/BLOCK_ID"

        # Determine correct texture name
        if block_id in mapping:
            correct_tex_name = mapping[block_id]
        else:
            correct_tex_name = block_id  # default: same as block name

        correct_tex = f"hbm:blocks/{correct_tex_name}"

        if correct_tex == current_tex:
            same += 1
            continue

        # Check if the texture file actually exists
        tex_file = os.path.join(TEX_BLOCKS_DIR, correct_tex_name + ".png")
        if not os.path.exists(tex_file):
            missing_tex += 1
            # Still update the JSON - texture may come later or be in a subpath

        data["textures"]["all"] = correct_tex
        with open(path, "w") as f:
            json.dump(data, f, indent=2)
        updated += 1

    print(f"Updated: {updated} model JSONs")
    print(f"Unchanged (already correct): {same}")
    print(f"Updated but texture file still missing: {missing_tex}")

    # Report blocks that map to a texture that doesn't exist
    print("\nSample of blocks still missing texture after fix:")
    count = 0
    for fname in sorted(os.listdir(MODELS_BLOCK)):
        if not fname.endswith(".json"):
            continue
        block_id = fname[:-5]
        tex_name = mapping.get(block_id, block_id)
        if not os.path.exists(os.path.join(TEX_BLOCKS_DIR, tex_name + ".png")):
            print(f"  {block_id} -> {tex_name}")
            count += 1
            if count >= 30:
                print("  ...")
                break

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    main()
