package com.hbm.render.block.ct;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

//~ stealth-todo: CTStitchReceiver buffers atlas-stitched texture fragments; render phase will impl receivers list + IconCT stitching
//~ context: client-side stub — fragCache stays null; no block-logic depends on this
// Bob's ctor: CTStitchReceiver(IIcon parentFull, IIcon parentCT) — removed (IIcon не в 1.12.2)
public class CTStitchReceiver {
    public TextureAtlasSprite[] fragCache = null;
}
