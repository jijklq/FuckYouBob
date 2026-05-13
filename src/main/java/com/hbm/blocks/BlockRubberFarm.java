package com.hbm.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;

public class BlockRubberFarm extends BlockFarmland {

    public BlockRubberFarm() {
        super();
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if(!this.isNearCCL(worldIn, pos) && !worldIn.isRainingAt(pos.up())) {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, 0), 2);
        } else {
            worldIn.setBlockState(pos, state.withProperty(MOISTURE, 7), 2);
        }
    }

    private boolean isNearCCL(World world, BlockPos pos) {
        for(int i = pos.getX() - 4; i <= pos.getX() + 4; ++i) {
            for(int j = pos.getY(); j <= pos.getY() + 1; ++j) {
                for(int k = pos.getZ() - 4; k <= pos.getZ() + 4; ++k) {
                    if(world.getBlockState(new BlockPos(i, j, k)).getBlock() == ModBlocks.ccl_block) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        return true;
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(this);
    }

    @Override
    public void onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance) {
        if(!worldIn.isRemote && worldIn.rand.nextFloat() < fallDistance - 0.5F) {
            if(!(entityIn instanceof EntityPlayer) && !worldIn.getGameRules().getBoolean("mobGriefing")) {
                return;
            }
            worldIn.setBlockState(pos, ModBlocks.rubber_silt.getDefaultState());
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if(worldIn.getBlockState(pos.up()).isOpaqueCube()) {
            worldIn.setBlockState(pos, ModBlocks.rubber_silt.getDefaultState());
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModBlocks.rubber_silt.getItemDropped(ModBlocks.rubber_silt.getDefaultState(), rand, fortune);
    }
}
