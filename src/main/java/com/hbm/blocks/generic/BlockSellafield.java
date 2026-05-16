package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.main.MainRegistry;
import com.hbm.potion.HbmPotion;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Random;

public class BlockSellafield extends BlockHazard {

    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 5);
    public static final int SELLAFITE_LEVELS = 6;

    public BlockSellafield(Material mat) {
        super(mat);
        this.setCreativeTab(MainRegistry.blockTab);
        this.setTickRandomly(true);
        this.rad = 0.5F;
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LEVEL, MathHelper.clamp(meta, 0, 5));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for(int i = 0; i < SELLAFITE_LEVELS; i++) {
            list.add(new ItemStack(this, 1, i));
        }
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if(!world.isRemote && entity instanceof EntityLivingBase) {
            if(HbmPotion.radiation == null) return;
            int level = world.getBlockState(pos).getValue(LEVEL);
            int amp = level < 5 ? level : level * 2;
            ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, amp));
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        int level = state.getValue(LEVEL);
        ChunkRadiationManager.proxy.incrementRad(world, pos, this.rad * (level + 1));
        if(rand.nextInt(level == 0 ? 25 : 15) == 0) {
            if(level > 0) {
                world.setBlockState(pos, state.withProperty(LEVEL, level - 1), 2);
            } else {
                world.setBlockState(pos, ModBlocks.sellafield_slaked.getDefaultState());
            }
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) { }
}
