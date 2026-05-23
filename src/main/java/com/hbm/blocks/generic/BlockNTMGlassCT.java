package com.hbm.blocks.generic;

import com.hbm.render.block.ct.IBlockCT;
import net.minecraft.block.material.Material;

//~ stealth-todo: full CT render (registerBlockIcons + CTStitchReceiver primeReceiver + getFragments impl) deferred to render phase
//~ context: thin wrapper над BlockNTMGlass parent (already in port); CT-shader render не функционирует в block-only фазе
// Bob's ctor: BlockNTMGlassCT(int layer, String name, Material material) — String name dropped (textures via blockstate JSON in 1.12.2)
// Bob's: getRenderType() returns CT.renderID — не override в 1.12.2 (default EnumBlockRenderType.MODEL)
// Bob's: rec (CTStitchReceiver) + registerBlockIcons — render-deferred
public class BlockNTMGlassCT extends BlockNTMGlass implements IBlockCT {

    public BlockNTMGlassCT(int layer, Material material) {
        super(layer, material);
    }

    public BlockNTMGlassCT(int layer, Material material, boolean doesDrop) {
        super(layer, material, doesDrop);
    }
}
