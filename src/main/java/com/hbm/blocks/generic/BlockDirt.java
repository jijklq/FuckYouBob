package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.saveddata.TomSaveData;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.util.Random;

public class BlockDirt extends Block {
    public BlockDirt(Material mat) { super(mat); }
    public BlockDirt(Material mat, boolean tick) { super(mat); this.setTickRandomly(tick); }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.DIRT);
    }

    /*@Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        for(int i = -1; i < 2; i++) {
            for(int j = -1; j < 2; j++) {
                for(int k = -1; k < 2; k++) {
                    Block b = world.getBlockState(pos.add(i, j, k)).getBlock();
                    if(b instanceof net.minecraft.block.BlockGrass) {
                        world.setBlockState(pos, Blocks.DIRT.getDefaultState());
                    }
                }
            }
        }
    }*/

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if(!world.isRemote) {
            TomSaveData data = TomSaveData.forWorld(world);
            int light = Math.max(world.getLightFor(EnumSkyBlock.BLOCK, pos.up()),
                                 (int) (world.getLight(pos.up()) * (1 - data.dust)));
            if(light >= 9 && data.fire == 0) {
                world.setBlockState(pos, Blocks.GRASS.getDefaultState());
                if(world.getBlockState(pos.down()).getBlock() == Blocks.DIRT)
                    world.setBlockState(pos.down(), ModBlocks.impact_dirt.getDefaultState());
            }
        }
    }
}
