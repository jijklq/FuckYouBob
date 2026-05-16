package com.hbm.blocks.generic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import java.util.ArrayList;
import java.util.List;

public class BlockGenericStairs extends BlockStairs {
    public static List<Object[]> recipeGen = new ArrayList<Object[]>();

    public BlockGenericStairs(Block block, int meta) {
        super(block.getStateFromMeta(meta));
        this.useNeighborBrightness = true;
        recipeGen.add(new Object[] {block, meta, this});
    }
}
