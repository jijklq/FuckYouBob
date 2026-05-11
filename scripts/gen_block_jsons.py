"""
Generate blockstate + model/block + model/item JSONs for all registered blocks.
Reads ModBlocks.java, extracts registry names, writes JSON files.
Uses cube_all (all 6 faces same texture) - correct for ores/simple blocks,
acceptable placeholder for machines until their TileEntity is ported.
"""

import re
import os
import json

MODBLOCKS = "src/main/java/com/hbm/blocks/ModBlocks.java"
OUT_BASE  = "src/main/resources/assets/hbm"

def main():
    with open(MODBLOCKS, encoding="utf-8") as f:
        src = f.read()

    names = sorted(set(re.findall(r'setRegistryName\("hbm",\s*"([^"]+)"\)', src)))
    print(f"Found {len(names)} unique block registry names")

    os.makedirs(f"{OUT_BASE}/blockstates",   exist_ok=True)
    os.makedirs(f"{OUT_BASE}/models/block",  exist_ok=True)
    os.makedirs(f"{OUT_BASE}/models/item",   exist_ok=True)

    skipped_item = 0
    for name in names:
        # blockstates/NAME.json
        bs = {"variants": {"normal": {"model": f"hbm:{name}"}}}
        with open(f"{OUT_BASE}/blockstates/{name}.json", "w") as f:
            json.dump(bs, f, indent=2)

        # models/block/NAME.json
        mb = {"parent": "block/cube_all", "textures": {"all": f"hbm:blocks/{name}"}}
        with open(f"{OUT_BASE}/models/block/{name}.json", "w") as f:
            json.dump(mb, f, indent=2)

        # models/item/NAME.json  — skip if already exists (item script ran first)
        item_path = f"{OUT_BASE}/models/item/{name}.json"
        if os.path.exists(item_path):
            skipped_item += 1
        else:
            mi = {"parent": f"hbm:block/{name}"}
            with open(item_path, "w") as f:
                json.dump(mi, f, indent=2)

    print(f"Written: {len(names)} blockstates, {len(names)} block models, "
          f"{len(names) - skipped_item} item models ({skipped_item} skipped, already exist)")

if __name__ == "__main__":
    os.chdir(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    main()
