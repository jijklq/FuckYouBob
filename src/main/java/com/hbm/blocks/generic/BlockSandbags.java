package com.hbm.blocks.generic;

import com.hbm.render.block.ISBRHUniversal;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

//~ stealth-todo: full custom render (Bob's renderInventoryBlock + renderWorldBlock via RenderBlocksNT) deferred to render phase;
//~ context: isConnected() simplified to same-block-only — Bob also checks isOpaqueCube/isNormalCube neighbours, restored at render phase
public class BlockSandbags extends Block implements ISBRHUniversal {

    public BlockSandbags(Material mat) {
        super(mat);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        float min = 0.25F, max = 0.75F;
        Block nx = world.getBlockState(pos.west()).getBlock();
        Block px = world.getBlockState(pos.east()).getBlock();
        Block nz = world.getBlockState(pos.north()).getBlock();
        Block pz = world.getBlockState(pos.south()).getBlock();
        float minX = (nx == this) ? 0F : min;
        float minZ = (nz == this) ? 0F : min;
        float maxX = (px == this) ? 1F : max;
        float maxZ = (pz == this) ? 1F : max;
        return new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ);
    }
}
