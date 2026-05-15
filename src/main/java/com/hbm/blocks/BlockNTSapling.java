package com.hbm.blocks;

import java.util.Locale;
import java.util.Random;

import com.hbm.dim.tekto.TTree;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.handler.atmosphere.IPlantableBreathing;
import com.hbm.inventory.fluid.Fluids;

import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class BlockNTSapling extends BlockBush implements IGrowable, IBlockMulti, IPlantableBreathing {

    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 1);

    public enum EnumSapling { VINYL, PVC }

    public BlockNTSapling() {
        super();
        setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, meta & 1);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT);
    }

    @Override
    public boolean canBreathe(CBT_Atmosphere atmosphere) {
        if (atmosphere == null) return false;
        return atmosphere.hasFluid(Fluids.TEKTOAIR, 0.1) || atmosphere.hasFluid(Fluids.CHLORINE, 0.1);
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        IBlockState soil = world.getBlockState(pos.down());
        net.minecraft.block.Block b = soil.getBlock();
        boolean lightOk = world.getLight(pos) >= 8 || world.canSeeSky(pos);
        boolean soilOk = b == ModBlocks.rubber_silt || b == ModBlocks.rubber_grass || b == ModBlocks.rubber_farmland
                || b.canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this);
        return lightOk && soilOk;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            super.updateTick(world, pos, state, rand);
            if (world.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
                this.grow(world, rand, pos, state);
            }
        }
    }

    private void generateTree(World world, BlockPos pos, IBlockState state, Random rand) {
        int variant = state.getValue(VARIANT);
        WorldGenAbstractTree treeGen;
        switch (variant) {
            case 0: treeGen = new TTree(false, 2, 4, 5, 3, 2, false, ModBlocks.vinyl_log, ModBlocks.pet_leaves); break;
            case 1: treeGen = new TTree(true, 2, 5, 7, 4, 3, false, ModBlocks.pvc_log, ModBlocks.rubber_leaves); break;
            default: return;
        }
        world.setBlockToAir(pos);
        if (!treeGen.generate(world, rand, pos)) {
            world.setBlockState(pos, state, 2);
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        generateTree(worldIn, pos, state, rand);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(VARIANT, stack.getMetadata() & 1), 2);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT);
    }

    @Override
    public int getSubCount() {
        return EnumSapling.values().length;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        EnumSapling sapling = EnumSapling.values()[stack.getMetadata() & 1];
        return this.getUnlocalizedName() + "_" + sapling.name().toLowerCase(Locale.US);
    }
}
