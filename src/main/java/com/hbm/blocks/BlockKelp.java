package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockKelp extends Block {

    // true = top piece (no kelp above, meta 8 in original), false = lower piece (kelp above, meta 0)
    public static final PropertyBool TOP = PropertyBool.create("top");

    public BlockKelp() {
        super(Material.WATER);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TOP, false));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TOP);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TOP, meta != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TOP) ? 8 : 0;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    private boolean canKelpStay(World world, BlockPos pos) {
        Block below = world.getBlockState(pos.down()).getBlock();
        return below == this || below == ModBlocks.laythe_silt;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        Block below = worldIn.getBlockState(pos.down()).getBlock();
        return below == this || below == ModBlocks.laythe_silt;
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        this.updateBlockMeta(worldIn, pos);
    }

    private void updateBlockMeta(World world, BlockPos pos) {
        boolean top = (world.getBlockState(pos.up()).getBlock() != this);
        world.setBlockState(pos, this.getDefaultState().withProperty(TOP, top), 2);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.saltleaf;
    }

    @Override
    public int quantityDropped(Random random) {
        return random.nextInt(4);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if(!this.canKelpStay(worldIn, pos)) {
            worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
        } else {
            this.updateBlockMeta(worldIn, pos);
        }
    }
}
