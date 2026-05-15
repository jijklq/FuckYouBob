package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRubberLeaves extends BlockLeaves {

    public BlockRubberLeaves() {
        super();
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(CHECK_DECAY, Boolean.FALSE)
                .withProperty(DECAYABLE, Boolean.TRUE));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CHECK_DECAY, DECAYABLE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(DECAYABLE, (meta & 4) == 0)
                .withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int i = 0;
        if (!state.getValue(DECAYABLE)) i |= 4;
        if (state.getValue(CHECK_DECAY)) i |= 8;
        return i;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return BlockPlanks.EnumType.OAK;
    }

    @Override
    public boolean isShearable(ItemStack item, IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return Lists.newArrayList(new ItemStack(this, 1, 0));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if(this == ModBlocks.pet_leaves) {
            return ModItems.leaf_pet;
        }
        return ModItems.leaf_rubber;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    protected boolean canSilkHarvest() {
        return false;
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);

        if(!world.isRemote) {
            Random rand = world.rand;
            if(this == ModBlocks.rubber_leaves && rand.nextFloat() < 0.3F) {
                spawnAsEntity(world, pos, new ItemStack(ModItems.leaf_rubber));
                if(rand.nextFloat() < 0.5F) {
                    spawnAsEntity(world, pos, new ItemStack(ModBlocks.sapling_pvc, 1, 1));
                }
            }
            if(this == ModBlocks.pet_leaves && rand.nextFloat() < 0.3F) {
                spawnAsEntity(world, pos, new ItemStack(ModItems.leaf_pet));
                if(rand.nextFloat() < 0.5F) {
                    spawnAsEntity(world, pos, new ItemStack(ModBlocks.sapling_pvc, 1, 0));
                }
            }
        }
    }
}
