package com.hbm.dim.tekto;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

// Stub — real tree generation ported in worldgen stage
public class TTree extends WorldGenAbstractTree {

    public TTree(boolean notify, int offset, int smallest, int tallest, int xz, int y, boolean vines, Block log, Block leaf) {
        super(notify);
    }

    @Override
    public boolean generate(World worldIn, Random rand, BlockPos pos) {
        return false;
    }
}
