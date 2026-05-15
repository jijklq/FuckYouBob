package com.hbm.blocks.generic;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.world.feature.HugeMush;

import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class BlockMush extends Block implements IGrowable, IPlantable {

    private static final AxisAlignedBB MUSH_AABB = new AxisAlignedBB(0.3D, 0.0D, 0.3D, 0.7D, 0.4D, 0.7D);

    public BlockMush(Material mat) {
        super(mat);
        this.setTickRandomly(true);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return MUSH_AABB;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    private boolean canBlockStay(World world, BlockPos pos) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            IBlockState soil = world.getBlockState(pos.down());
            return soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this)
                    || canMushGrowHere(world, pos.getX(), pos.getY(), pos.getZ());
        }
        return false;
    }

    private static final Set<Block> canGrowOn = new HashSet<Block>();

    public boolean canMushGrowHere(World world, int x, int y, int z) {
        if (canGrowOn.isEmpty()) {
            canGrowOn.add(ModBlocks.waste_earth);
            canGrowOn.add(ModBlocks.waste_mycelium);
            canGrowOn.add(ModBlocks.waste_trinitite);
            canGrowOn.add(ModBlocks.waste_trinitite_red);
            canGrowOn.add(ModBlocks.block_waste);
            canGrowOn.add(ModBlocks.block_waste_painted);
            canGrowOn.add(ModBlocks.block_waste_vitrified);
        }
        Block block = world.getBlockState(new BlockPos(x, y - 1, z)).getBlock();
        return canGrowOn.contains(block);
    }

    public boolean growHuge(World world, BlockPos pos, Random rand) {
        world.setBlockToAir(pos);
        new HugeMush().generate(world, rand, pos);
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && this.canBlockStay(worldIn, pos);
    }

    protected void checkAndDropBlock(World world, BlockPos pos) {
        if (!this.canBlockStay(world, pos)) {
            this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        this.checkAndDropBlock(worldIn, pos);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        this.checkAndDropBlock(world, pos);

        if (GeneralConfig.enableMycelium && world.getBlockState(pos.down()).getBlock() == ModBlocks.waste_earth && rand.nextInt(5) == 0) {
            world.setBlockState(pos.down(), ModBlocks.waste_mycelium.getDefaultState());
        }

        if (rand.nextInt(25) == 0) {
            byte range = 4;
            int maxShroom = 3;
            int ix, iy, iz;

            for (ix = x - range; ix <= x + range; ++ix) {
                for (iy = y - range; iy <= y + range; ++iy) {
                    for (iz = z - 1; iz <= z + 1; ++iz) {
                        if (world.getBlockState(new BlockPos(ix, iz, iy)).getBlock() == this) {
                            --maxShroom;
                            if (maxShroom <= 0) return;
                        }
                    }
                }
            }

            ix = x + rand.nextInt(5) - 2;
            iy = z + rand.nextInt(2) - rand.nextInt(2);
            iz = y + rand.nextInt(5) - 2;

            for (int l1 = 0; l1 < 4; ++l1) {
                if (world.isAirBlock(new BlockPos(ix, iy, iz)) && this.canMushGrowHere(world, ix, iy, iz)) {
                    x = ix; z = iy; y = iz;
                }
                ix = x + rand.nextInt(5) - 2;
                iy = z + rand.nextInt(2) - rand.nextInt(2);
                iz = y + rand.nextInt(5) - 2;
            }

            if (world.isAirBlock(new BlockPos(ix, iy, iz)) && this.canMushGrowHere(world, ix, iy, iz)) {
                world.setBlockState(new BlockPos(ix, iy, iz), this.getDefaultState(), 2);
            }
        }
    }

    @Override
    public boolean canGrow(World worldIn, BlockPos pos, IBlockState state, boolean isClient) {
        return canBlockStay(worldIn, pos);
    }

    @Override
    public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        return rand.nextFloat() < 0.4F;
    }

    @Override
    public void grow(World worldIn, Random rand, BlockPos pos, IBlockState state) {
        this.growHuge(worldIn, pos, rand);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Cave;
    }

    @Override
    public IBlockState getPlant(IBlockAccess world, BlockPos pos) {
        return this.getDefaultState();
    }
}
