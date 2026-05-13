package com.hbm.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.IPlantableBreathing;
import com.hbm.items.ItemEnums.EnumTarType;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

public class BlockCrop extends BlockBush implements IGrowable, IPlantableBreathing {

    public static final PropertyInteger AGE = PropertyInteger.create("age", 0, 7);
    private static final AxisAlignedBB CROP_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.25D, 1.0D);

    protected int maxGrowthStage = 7;
    protected Block soilBlock;

    private final Predicate<CBT_Atmosphere> atmospherePredicate;
    public boolean canHydro;

    public BlockCrop(Block block, Predicate<CBT_Atmosphere> atmospherePredicate, boolean canHydro) {
        setTickRandomly(true);
        setHardness(0.0F);
        setSoundType(SoundType.PLANT);
        this.setDefaultState(this.blockState.getBaseState().withProperty(AGE, 0));
        this.soilBlock = block;
        this.atmospherePredicate = atmospherePredicate;
        this.canHydro = canHydro;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AGE);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(AGE, MathHelper.clamp(meta, 0, 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(AGE);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return CROP_AABB;
    }

    @Override
    public boolean canBreathe(CBT_Atmosphere atmosphere) {
        if(atmosphere == null) return false;
        return this.atmospherePredicate.test(atmosphere);
    }

    @Override
    public boolean canBlockStay(World worldIn, BlockPos pos, IBlockState state) {
        IBlockState soil = worldIn.getBlockState(pos.down());
        return soil.getBlock().canSustainPlant(soil, worldIn, pos.down(), EnumFacing.UP, this);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }

    public void incrementGrowStage(World world, Random rand, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        int growStage = state.getValue(AGE) + MathHelper.getInt(rand, 2, 5);
        if(growStage > maxGrowthStage) growStage = maxGrowthStage;
        world.setBlockState(pos, state.withProperty(AGE, growStage), 2);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if(this == ModBlocks.crop_strawberry) return ModItems.strawberry;
        if(this == ModBlocks.crop_mint)       return ModItems.mint_leaves;
        if(this == ModBlocks.crop_coffee)     return ModItems.bean_raw;
        if(this == ModBlocks.crop_tea)        return state.getValue(AGE) == 7 ? ModItems.tea_leaf : ModItems.teaseeds;
        if(this == ModBlocks.crop_paraffin)   return ModItems.paraffin_seeds;
        return Item.getItemFromBlock(this);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(worldIn, pos, state, rand);
        int growStage = state.getValue(AGE) + 1;
        if(growStage > 7) growStage = 7;
        worldIn.setBlockState(pos, state.withProperty(AGE, growStage), 2);
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(AGE) != 7;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        incrementGrowStage(worldIn, rand, pos);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        int meta = state.getValue(AGE);
        Random rand = world instanceof World ? ((World) world).rand : new Random();

        List<ItemStack> drops = new ArrayList<>();
        int qty = (meta == 7) ? 4 : meta / 2;
        Item item = getItemDropped(state, rand, fortune);
        if(item != null && qty > 0) {
            drops.add(new ItemStack(item, qty));
        }

        if(this == ModBlocks.crop_tea && meta >= 7) {
            for(int i = 0; i < 3 + fortune; ++i) {
                if(rand.nextInt(15) <= meta) {
                    drops.add(new ItemStack(ModItems.teaseeds, 1, 0));
                }
            }
        }
        if(this == ModBlocks.crop_paraffin && meta >= 7) {
            for(int i = 0; i < 3 + fortune; ++i) {
                if(rand.nextInt(15) <= meta) {
                    drops.add(new ItemStack(ModItems.paraffin_seeds));
                    drops.add(new ItemStack(ModItems.oil_tar, 1, EnumTarType.WAX.ordinal()));
                }
            }
        }

        return drops;
    }
}
