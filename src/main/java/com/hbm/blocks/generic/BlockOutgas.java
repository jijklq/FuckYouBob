package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockOutgas extends Block {
    boolean randomTick;
    int rate;
    boolean onBreak;
    boolean onNeighbour;

    public BlockOutgas(Material mat, boolean randomTick, int rate, boolean onBreak) {
        super(mat);
        this.setTickRandomly(randomTick);
        this.randomTick = randomTick;
        this.rate = rate;
        this.onBreak = onBreak;
        this.onNeighbour = false;
    }

    public BlockOutgas(Material mat, boolean randomTick, int rate, boolean onBreak, boolean onNeighbour) {
        this(mat, randomTick, rate, onBreak);
        this.onNeighbour = onNeighbour;
    }

    public int tickRate(World world) { return rate; }

    protected Block getGas() {
        if(this == ModBlocks.ore_uranium_scorched ||
                this == ModBlocks.ore_gneiss_uranium || this == ModBlocks.ore_gneiss_uranium_scorched ||
                this == ModBlocks.ore_nether_uranium || this == ModBlocks.ore_nether_uranium_scorched) {
            return ModBlocks.gas_radon;
        }
        if(this == ModBlocks.block_corium_cobble) return ModBlocks.gas_radon;
        if(this == ModBlocks.ancient_scrap) return ModBlocks.gas_radon_tomb;
        if(this == ModBlocks.ore_nether_coal) return ModBlocks.gas_monoxide;
        if(this == ModBlocks.ore_gneiss_asbestos ||
                this == ModBlocks.block_asbestos || this == ModBlocks.deco_asbestos ||
                this == ModBlocks.brick_asbestos || this == ModBlocks.tile_lab ||
                this == ModBlocks.tile_lab_cracked || this == ModBlocks.tile_lab_broken) {
            return ModBlocks.gas_asbestos;
        }
        return Blocks.AIR;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        if(fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.getDefaultState(), rand, fortune)) {
            int mult = rand.nextInt(fortune + 2) - 1;
            if(mult < 0) mult = 0;
            return this.quantityDropped(rand) * (mult + 1);
        } else {
            return this.quantityDropped(rand);
        }
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if(this.randomTick && getGas() == ModBlocks.gas_asbestos) {
            if(world.getBlockState(pos.up()).getBlock() == Blocks.AIR) {
                if(world.rand.nextInt(10) == 0)
                    world.setBlockState(pos.up(), ModBlocks.gas_asbestos.getDefaultState());
                for(int i = 0; i < 5; i++)
                    world.spawnParticle(EnumParticleTypes.TOWN_AURA,
                            pos.getX() + world.rand.nextFloat(), pos.getY() + 1.1, pos.getZ() + world.rand.nextFloat(), 0, 0, 0);
            }
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        EnumFacing dir = EnumFacing.VALUES[rand.nextInt(6)];
        BlockPos target = pos.offset(dir);
        if(world.getBlockState(target).getBlock() == Blocks.AIR) {
            world.setBlockState(target, getGas().getDefaultState());
        }
    }

    @Override
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        if(onBreak) world.setBlockState(pos, this.getGas().getDefaultState());
        super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        if(onNeighbour && world.rand.nextInt(3) == 0) {
            for(EnumFacing dir : EnumFacing.VALUES) {
                BlockPos target = pos.offset(dir);
                if(world.getBlockState(target).getBlock() == Blocks.AIR) {
                    world.setBlockState(target, getGas().getDefaultState());
                }
            }
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        super.breakBlock(world, pos, state);
        if(this == ModBlocks.ancient_scrap) {
            for(int ix = -2; ix <= 2; ix++) {
                for(int iy = -2; iy <= 2; iy++) {
                    for(int iz = -2; iz <= 2; iz++) {
                        if(Math.abs(ix + iy + iz) < 5 && Math.abs(ix + iy + iz) > 0 &&
                                world.getBlockState(pos.add(ix, iy, iz)).getBlock() == Blocks.AIR) {
                            world.setBlockState(pos.add(ix, iy, iz), this.getGas().getDefaultState());
                        }
                    }
                }
            }
        }
    }
}
