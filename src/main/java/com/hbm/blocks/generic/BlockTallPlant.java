package com.hbm.blocks.generic;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockDeadPlant.EnumDeadPlantType;
import com.hbm.blocks.generic.BlockNTMFlower.EnumFlowerType;
import com.hbm.items.ItemEnums;
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

public class BlockTallPlant extends BlockEnumMulti implements IPlantable, IGrowable {

    public static final PropertyInteger TYPE = PropertyInteger.create("type", 0, 3);
    public static final PropertyBool HALF = PropertyBool.create("half");

    public enum EnumTallFlower {
        WEED(false),
        CD2(true),
        CD3(true),
        CD4(true);

        public boolean needsOil;
        EnumTallFlower(boolean needsOil) { this.needsOil = needsOil; }
    }

    public BlockTallPlant() {
        super(Material.PLANTS, EnumTallFlower.class, false);
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
        return this.getDefaultState().withProperty(TYPE, meta & 3).withProperty(HALF, meta >= 8);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE) + (state.getValue(HALF) ? 8 : 0);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    private boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(HALF)) {
            IBlockState below = world.getBlockState(pos.down());
            return below.getBlock() == this && !below.getValue(HALF) && below.getValue(TYPE).equals(state.getValue(TYPE));
        }
        return BlockNTMFlower.canGrowOnSoil(world.getBlockState(pos.down()).getBlock());
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return super.canPlaceBlockAt(world, pos) && BlockNTMFlower.canGrowOnSoil(world.getBlockState(pos.down()).getBlock()) && world.isAirBlock(pos.up());
    }

    public static boolean detectCut = true;

    protected void checkAndDropBlock(World world, BlockPos pos, IBlockState state) {
        if (!canBlockStay(world, pos, state)) {
            if (!state.getValue(HALF)) {
                this.dropBlockAsItem(world, pos, state, 0);
            }
            world.setBlockToAir(pos);
        }

        if (!detectCut) return;

        if (!state.getValue(HALF)) {
            IBlockState above = world.getBlockState(pos.up());
            if ((above.getBlock() != this || !above.getValue(HALF) || !above.getValue(TYPE).equals(state.getValue(TYPE)))
                    && BlockNTMFlower.canGrowOnSoil(world.getBlockState(pos.down()).getBlock())) {
                int type = state.getValue(TYPE);
                if (type == EnumTallFlower.WEED.ordinal()) {
                    world.setBlockState(pos, ModBlocks.plant_flower.getStateFromMeta(EnumFlowerType.WEED.ordinal()), 3);
                } else {
                    world.setBlockState(pos, ModBlocks.plant_flower.getStateFromMeta(EnumFlowerType.CD0.ordinal()), 3);
                }
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        super.neighborChanged(state, world, pos, block, fromPos);
        checkAndDropBlock(world, pos, state);
    }

    @Override
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        if (!state.getValue(HALF) && world.getBlockState(pos.up()).getBlock() == this) {
            if (player.capabilities.isCreativeMode) {
                world.setBlockToAir(pos.up());
            } else {
                this.dropBlockAsItem(world, pos.up(), world.getBlockState(pos.up()), 0);
                world.setBlockToAir(pos.up());
            }
        }
        super.onBlockHarvested(world, pos, state, player);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        int type = stack.getMetadata() & 3;
        world.setBlockState(pos.up(), this.getDefaultState().withProperty(TYPE, type).withProperty(HALF, true), 2);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isRemote) return;
        if (state.getValue(HALF)) return;

        int rec = rectify(state.getValue(TYPE));
        Block below = world.getBlockState(pos.down()).getBlock();

        if (!EnumTallFlower.values()[rec].needsOil) {
            if (below == ModBlocks.dirt_dead || below == ModBlocks.dirt_oily) {
                world.setBlockState(pos, ModBlocks.plant_dead.getStateFromMeta(EnumDeadPlantType.BIGFLOWER.ordinal()), 3);
                return;
            }
        }

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
        int rec = rectify(state.getValue(TYPE));
        boolean isTop = state.getValue(HALF);

        if (rec == EnumTallFlower.CD2.ordinal() || rec == EnumTallFlower.CD3.ordinal()) {
            BlockPos soilPos = isTop ? pos.down().down() : pos.down();
            if (!hasWaterNearby(world, soilPos)) return false;
        }

        if (rec == EnumTallFlower.CD3.ordinal()) {
            BlockPos soilPos = isTop ? pos.down().down() : pos.down();
            Block onSoil = world.getBlockState(soilPos).getBlock();
            return onSoil == ModBlocks.dirt_dead || onSoil == ModBlocks.dirt_oily;
        }

        return rec != EnumTallFlower.CD4.ordinal() && rec != EnumTallFlower.WEED.ordinal();
    }

    @Override
    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        int rec = rectify(state.getValue(TYPE));
        if (rec == EnumTallFlower.CD3.ordinal()) return true;
        return rand.nextFloat() < 0.33F;
    }

    @Override
    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        int rec = rectify(state.getValue(TYPE));
        boolean isTop = state.getValue(HALF);

        detectCut = false;

        if (rec == EnumTallFlower.CD2.ordinal() || rec == EnumTallFlower.CD3.ordinal()) {
            IBlockState newBottom = this.getDefaultState().withProperty(TYPE, rec + 1).withProperty(HALF, false);
            IBlockState newTop = this.getDefaultState().withProperty(TYPE, rec + 1).withProperty(HALF, true);
            if (!isTop) {
                world.setBlockState(pos.up(), newTop, 3);
                world.setBlockState(pos, newBottom, 3);
                if (rec == EnumTallFlower.CD3.ordinal()) world.setBlockState(pos.down(), Blocks.DIRT.getDefaultState());
            } else {
                world.setBlockState(pos, newTop, 3);
                world.setBlockState(pos.down(), newBottom, 3);
                if (rec == EnumTallFlower.CD3.ordinal()) world.setBlockState(pos.down().down(), Blocks.DIRT.getDefaultState());
            }
        }

        detectCut = true;
    }

    @Override
    public int damageDropped(IBlockState state) {
        int rec = rectify(state.getValue(TYPE));
        if (rec == EnumTallFlower.WEED.ordinal()) return EnumFlowerType.WEED.ordinal();
        return EnumFlowerType.CD0.ordinal();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(ModBlocks.plant_flower);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        List<ItemStack> ret = super.getDrops(world, pos, state, fortune);
        if (state.getValue(HALF) && state.getValue(TYPE) == EnumTallFlower.CD4.ordinal()) {
            Random rand = world instanceof World ? ((World) world).rand : new Random();
            ret.add(new ItemStack(ModItems.plant_item, 3 + rand.nextInt(4), ItemEnums.EnumPlantType.MUSTARDWILLOW.ordinal()));
        }
        return ret;
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) { return EnumPlantType.Plains; }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) { return this.getDefaultState(); }
}
