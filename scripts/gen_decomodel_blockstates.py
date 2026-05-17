import json
from pathlib import Path

YAW = {"north": 0, "south": 180, "west": 270, "east": 90}

specs = [
    ("deco_crt",      4),
    ("deco_toaster",  3),
    ("deco_computer", 2),  # 0..1 workaround; only 0 active
]

out = Path("src/main/resources/assets/hbm/blockstates")

for name, n_variants in specs:
    variants = {}
    for v in range(n_variants):
        for fname, yaw in YAW.items():
            key = f"variant={v},facing={fname}"
            entry = {"model": f"hbm:{name}"}
            if yaw != 0:
                entry["y"] = yaw
            variants[key] = entry
    (out / f"{name}.json").write_text(json.dumps({"variants": variants}, indent=2), encoding="utf-8")

print("OK: 3 blockstate JSONs generated (16 + 12 + 8 entries)")
