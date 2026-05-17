import json
from pathlib import Path

PIPE_FIELDS = [
    "deco_pipe", "deco_pipe_rusted", "deco_pipe_green", "deco_pipe_green_rusted", "deco_pipe_red", "deco_pipe_marked",
    "deco_pipe_rim", "deco_pipe_rim_rusted", "deco_pipe_rim_green", "deco_pipe_rim_green_rusted", "deco_pipe_rim_red", "deco_pipe_rim_marked",
    "deco_pipe_framed", "deco_pipe_framed_rusted", "deco_pipe_framed_green", "deco_pipe_framed_green_rusted", "deco_pipe_framed_red", "deco_pipe_framed_marked",
    "deco_pipe_quad", "deco_pipe_quad_rusted", "deco_pipe_quad_green", "deco_pipe_quad_green_rusted", "deco_pipe_quad_red", "deco_pipe_quad_marked",
]

out_dir = Path("src/main/resources/assets/hbm/blockstates")

for name in PIPE_FIELDS:
    data = {
        "variants": {
            "axis=x": {"model": f"hbm:{name}", "x": 90, "y": 90},
            "axis=y": {"model": f"hbm:{name}"},
            "axis=z": {"model": f"hbm:{name}", "x": 90}
        }
    }
    (out_dir / f"{name}.json").write_text(json.dumps(data, indent=2), encoding="utf-8")

print(f"OK: {len(PIPE_FIELDS)} blockstate JSONs generated")
