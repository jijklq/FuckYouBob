package com.hbm.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFallingNT extends Block {

    public BlockFallingNT() {
        super(Material.SAND);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    public BlockFallingNT(Material mat) {
        super(mat);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        world.scheduleBlockUpdate(pos, this, this.tickRate(world), 1);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        world.scheduleBlockUpdate(pos, this, this.tickRate(world), 1);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            this.fall(world, pos);
        }
    }

    protected void fall(World world, BlockPos pos) {
        if (canFallThrough(world, pos.down()) && pos.getY() >= 0) {
            int range = 32;
            BlockPos min = pos.add(-range, -range, -range);
            BlockPos max = pos.add(range, range, range);
            if (!BlockFalling.fallInstantly && world.isAreaLoaded(min, max)) {
                if (!world.isRemote) {
                    IBlockState state = world.getBlockState(pos);
                    EntityFallingBlock entity = new EntityFallingBlock(world,
                            pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, state);
                    this.modifyFallingBlock(entity);
                    world.spawnEntity(entity);
                }
            } else {
                IBlockState state = world.getBlockState(pos);
                world.setBlockToAir(pos);
                BlockPos cur = pos.down();
                while (canFallThrough(world, cur) && cur.getY() > 0) cur = cur.down();
                if (cur.getY() > 0) world.setBlockState(cur.up(), state);
            }
        }
    }

    protected void modifyFallingBlock(EntityFallingBlock falling) { }

    @Override
    public int tickRate(World world) {
        return 2;
    }

    public static boolean canFallThrough(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.isAir(state, world, pos)) return true;
        if (block == Blocks.FIRE) return true;
        Material mat = state.getMaterial();
        return mat == Material.WATER || mat == Material.LAVA;
    }

    protected void onLand(World world, BlockPos pos) { }
}
