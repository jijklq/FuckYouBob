package com.hbm.blocks.generic;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.generic.BlockDeadPlant.EnumDeadPlantType;
import com.hbm.blocks.generic.BlockTallPlant.EnumTallFlower;
import com.hbm.items.ModItems;
import com.hbm.items.ItemEnums;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockNTMFlower extends BlockEnumMulti implements IPlantable, IGrowable, ITooltipProvider {

    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 7);

    public enum EnumFlowerType {
        FOXGLOVE(false),
        TOBACCO(false),
        NIGHTSHADE(false),
        WEED(false),
        CD0(true),
        CD1(true),
        STRAWBERRY(false),
        MINT(false);

        public boolean needsOil;
        EnumFlowerType(boolean needsOil) { this.needsOil = needsOil; }
    }

    public BlockNTMFlower() {
        super(Material.PLANTS, EnumFlowerType.class, false);
        this.setTickRandomly(true);
    }

    @Override
    protected PropertyInteger getVariantProperty() { return VARIANT; }

    public static boolean canGrowOnSoil(Block block) {
        return block == Blocks.GRASS || block == Blocks.DIRT || block == Blocks.FARMLAND
                || block == ModBlocks.dirt_dead || block == ModBlocks.dirt_oily;
    }

    private boolean canBlockStay(World world, BlockPos pos) {
        return canGrowOnSoil(world.getBlockState(pos.down()).getBlock());
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && canBlockStay(world, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        checkAndDropBlock(world, pos, state);
    }

    protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {
        if (!canBlockStay(world, pos)) {
            this.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public int damageDropped(IBlockState state) {
        int variant = state.getValue(VARIANT);
        if (variant == EnumFlowerType.CD1.ordinal()) return EnumFlowerType.CD0.ordinal();
        return variant;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        int variant = state.getValue(VARIANT);
        EnumFlowerType type = EnumFlowerType.values()[rectify(variant)];
        if (!(type == EnumFlowerType.WEED || type == EnumFlowerType.CD0 || type == EnumFlowerType.CD1)) return;
        if (canGrow(world, pos, state, false) && canUseBonemeal(world, rand, pos, state) && rand.nextInt(3) == 0) {
            grow(world, rand, pos, state);
        }
    }

    private boolean hasWaterNearby(World world, BlockPos soilPos) {
        return world.getBlockState(soilPos.east()).getMaterial() == Material.WATER ||
               world.getBlockState(soilPos.west()).getMaterial() == Material.WATER ||
               world.getBlockState(soilPos.south()).getMaterial() == Material.WATER ||
               world.getBlockState(soilPos.north()).getMaterial() == Material.WATER;
    }

    @Override
    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        int variant = state.getValue(VARIANT);
        if (variant == EnumFlowerType.CD0.ordinal() || variant == EnumFlowerType.CD1.ordinal()) {
            if (!hasWaterNearby(world, pos.down())) return false;
        }
        if (variant == EnumFlowerType.WEED.ordinal() || variant == EnumFlowerType.CD1.ordinal()) {
            return world.isAirBlock(pos.up());
        }
        return true;
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        int variant = state.getValue(VARIANT);
        if (variant == EnumFlowerType.WEED.ordinal() || variant == EnumFlowerType.CD0.ordinal() || variant == EnumFlowerType.CD1.ordinal()) {
            return rand.nextFloat() < 0.33F;
        }
        return true;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int variant = state.getValue(VARIANT);
        Block below = world.getBlockState(pos.down()).getBlock();

        if (variant == EnumFlowerType.WEED.ordinal()) {
            if (below == ModBlocks.dirt_dead || below == ModBlocks.dirt_oily) {
                world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.GENERIC.ordinal()), 3);
                return;
            }
            world.setBlockState(pos, ModBlocks.plant_tall.getStateFromMeta(EnumTallFlower.WEED.ordinal()), 3);
            world.setBlockState(pos.up(), ModBlocks.plant_tall.getStateFromMeta(EnumTallFlower.WEED.ordinal() + 8), 3);
            return;
        }

        if (variant == EnumFlowerType.CD0.ordinal()) {
            world.setBlockState(pos, this.getStateFromMeta(EnumFlowerType.CD1.ordinal()), 3);
            return;
        }

        if (variant == EnumFlowerType.CD1.ordinal()) {
            world.setBlockState(pos, ModBlocks.plant_tall.getStateFromMeta(EnumTallFlower.CD2.ordinal()), 3);
            world.setBlockState(pos.up(), ModBlocks.plant_tall.getStateFromMeta(EnumTallFlower.CD2.ordinal() + 8), 3);
            return;
        }

        this.dropBlockAsItem(world, pos, state, 0);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        int variant = state.getValue(VARIANT);
        if (variant == EnumFlowerType.STRAWBERRY.ordinal()) return ModItems.strawberry;
        if (variant == EnumFlowerType.MINT.ordinal()) return ModItems.mint_leaves;
        return super.getItemDropped(state, rand, fortune);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int variant = state.getValue(VARIANT);
        if (variant == EnumFlowerType.STRAWBERRY.ordinal()) {
            Random rand = world instanceof World ? ((World) world).rand : new Random();
            return ModBlocks.getDropsWithoutDamage(this, state, fortune, rand);
        }
        return super.getDrops(world, pos, state, fortune);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) { return EnumPlantType.Plains; }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) { return this.getDefaultState(); }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) { }
}
