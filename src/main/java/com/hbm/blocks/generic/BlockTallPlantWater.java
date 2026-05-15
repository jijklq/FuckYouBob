package com.hbm.blocks.generic;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockTallPlantWater extends BlockEnumMulti implements IPlantable, IGrowable {

    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 1);
    public static final PropertyBool HALF = PropertyBool.create("half");

    public enum EnumTallPlantWater {
        LAYTHE;
    }

    public BlockTallPlantWater() {
        super(Material.GLASS, EnumTallPlantWater.class, false);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, 0).withProperty(HALF, false));
    }

    @Override
    protected PropertyInteger getVariantProperty() { return TYPE; }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE, HALF);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, 0).withProperty(HALF, meta >= 8);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HALF) ? 8 : 0;
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        // TYPE is always 0 (LAYTHE); clamp any stray value back to 0
        return state.withProperty(TYPE, 0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    private boolean canPlaceBlockOn(Block block) {
        return block == ModBlocks.laythe_silt;
    }

    private boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(HALF)) {
            IBlockState below = world.getBlockState(pos.down());
            return below.getBlock() == this && !below.getValue(HALF);
        }
        return canPlaceBlockOn(world.getBlockState(pos.down()).getBlock());
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos)
                && canPlaceBlockOn(world.getBlockState(pos.down()).getBlock())
                && world.getBlockState(pos.up()).getMaterial().isLiquid();
    }

    protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {
        if (!canBlockStay(world, pos, state)) {
            if (!state.getValue(HALF)) {
                this.dropBlockAsItem(world, pos, state, 0);
            }
            world.setBlockState(pos, Blocks.WATER.getDefaultState(), 3);
        }
        // Cut-detection omitted: in the original, ModBlocks.plant_flower.canBlockStay was always
        // false on laythe_silt (not a valid soil), so the cut-detection block was dead code for
        // the LAYTHE variant. Orphaned lower halves simply stay until manually broken.
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        checkAndDropBlock(world, pos, state);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (state.getValue(HALF)) {
            if (world.getBlockState(pos.down()).getBlock() == this) {
                world.setBlockToAir(pos.down());
            }
        } else {
            if (world.getBlockState(pos.up()).getBlock() == this) {
                if (player.capabilities.isCreativeMode) {
                    world.setBlockToAir(pos.up());
                } else {
                    this.dropBlockAsItem(world, pos.up(), world.getBlockState(pos.up()), 0);
                    world.setBlockToAir(pos.up());
                }
            }
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos.up(), this.getDefaultState().withProperty(TYPE, 0).withProperty(HALF, true), 2);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) { }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) { return false; }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) { return false; }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) { }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.saltleaf;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (state.getValue(HALF)) return Collections.emptyList();
        Random rand = world instanceof World ? ((World) world).rand : new Random();
        int qty = rand.nextInt(4);
        if (qty == 0) return Collections.emptyList();
        return Collections.singletonList(new ItemStack(ModItems.saltleaf, qty));
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) { return EnumPlantType.Plains; }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) { return this.getDefaultState(); }
}
