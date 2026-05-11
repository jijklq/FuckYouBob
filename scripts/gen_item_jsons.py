"""
Generate models/item JSONs for all registered items.
Reads ModItems.java, extracts registry names, writes item/generated JSON files.
Skips names already covered by gen_block_jsons.py (ItemBlocks share the name).
"""

import re
import os
import json

MODITEMS = "src/main/java/com/hbm/items/ModItems.java"
OUT_BASE  = "src/main/resources/assets/hbm"

def main():
    with open(MODITEMS, encoding="utf-8") as f:
        src = f.read()

    names = sorted(set(re.findall(r'setRegistryName\("hbm",\s*"([^"]+)"\)', src)))
    print(f"Found {len(names)} unique item registry names")

    os.makedirs(f"{OUT_BASE}/models/item", exist_ok=True)

    written = skipped = 0
    for name in names:
        item_path = f"{OUT_BASE}/models/item/{name}.json"
        if os.path.exists(item_path):
            skipped += 1
            continue
        mi = {
            "parent": "item/generated",
            "textures": {"layer0": f"hbm:items/{name}"}
        }
        with open(item_path, "w") as f:
            json.dump(mi, f, indent=2)
        written += 1

    print(f"Written: {written} item models, skipped {skipped} (already exist from block script)")

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    main()
