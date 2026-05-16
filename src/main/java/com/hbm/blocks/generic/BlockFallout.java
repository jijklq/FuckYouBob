package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmLivingProps.ContaminationEffect;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFallout extends Block {

    protected static final AxisAlignedBB FALLOUT_AABB =
        new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);

    public BlockFallout(Material mat) {
        super(mat);
        if (this == ModBlocks.salted_fallout) {
            this.setTickRandomly(true);
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return FALLOUT_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.fallout;
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        BlockPos below = pos.down();
        IBlockState belowState = world.getBlockState(below);
        Block block = belowState.getBlock();
        if (block == Blocks.ICE || block == Blocks.PACKED_ICE) return false;
        if (block.isLeaves(belowState, world, below) && !block.isAir(belowState, world, below)) return true;
        if (block == this && (block.getMetaFromState(belowState) & 7) == 7) return true;
        return belowState.isOpaqueCube() && belowState.getMaterial().blocksMovement();
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote && entity instanceof EntityLivingBase) {
            if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) return;
            if (HbmPotion.radiation == null) return; // TODO radiation-этап
            PotionEffect effect = new PotionEffect(HbmPotion.radiation, 10 * 60 * 20, 0);
            effect.setCurativeItems(new ArrayList<ItemStack>());
            ((EntityLivingBase) entity).addPotionEffect(effect);
        }
    }

    @Override
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        if (!world.isRemote) {
            HbmLivingProps.addCont(player, new ContaminationEffect(1F, 200, false));
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        this.checkPlacement(world, pos);
    }

    private boolean checkPlacement(World world, BlockPos pos) {
        if (!this.canPlaceBlockAt(world, pos)) {
            world.setBlockToAir(pos);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
    }

    @Override
    public boolean isReplaceable(IBlockAccess world, BlockPos pos) { return true; }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this == ModBlocks.salted_fallout) {
            ChunkRadiationManager.proxy.incrementRad(world, pos, 50);
        }
    }
}
