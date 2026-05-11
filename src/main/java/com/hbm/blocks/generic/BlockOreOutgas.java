package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockOreOutgas extends BlockOre {

	boolean randomTick;
	int rate;
	boolean onBreak;
	boolean onNeighbour;

	public BlockOreOutgas(Material mat, boolean randomTick, int rate, boolean onBreak) {
		super(mat);
		this.setTickRandomly(randomTick);
		this.randomTick = randomTick;
		this.rate = rate;
		this.onBreak = onBreak;
		this.onNeighbour = false;
	}

	public BlockOreOutgas(Material mat, boolean randomTick, int rate, boolean onBreak, boolean onNeighbour) {
		this(mat, randomTick, rate, onBreak);
		this.onNeighbour = onNeighbour;
	}

	@Override
	public int tickRate(World world) {
		return rate;
	}

	protected Block getGas() {
		if(this == ModBlocks.ore_uranium ||
				this == ModBlocks.ore_uranium_scorched ||
				this == ModBlocks.ore_nether_uranium ||
				this == ModBlocks.ore_nether_uranium_scorched ||
				this == ModBlocks.ore_gneiss_uranium ||
				this == ModBlocks.ore_gneiss_uranium_scorched ||
				this == ModBlocks.ore_sellafield_uranium_scorched) {
			return ModBlocks.gas_radon;
		}

		if(this == ModBlocks.ore_asbestos ||
				this == ModBlocks.ore_gneiss_asbestos) {
			return ModBlocks.gas_asbestos;
		}

		return Blocks.AIR;
	}

	@Override
	public void onEntityWalk(World world, BlockPos pos, Entity entity) {

		if(this.randomTick && getGas() == ModBlocks.gas_asbestos) {

			if(world.getBlockState(pos.up()).getBlock() == Blocks.AIR) {

				if(world.rand.nextInt(10) == 0)
					world.setBlockState(pos.up(), ModBlocks.gas_asbestos.getDefaultState());

				for(int i = 0; i < 5; i++)
					world.spawnParticle(EnumParticleTypes.TOWN_AURA,
							pos.getX() + world.rand.nextFloat(), pos.getY() + 1.1D, pos.getZ() + world.rand.nextFloat(),
							0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		EnumFacing dir = EnumFacing.values()[rand.nextInt(6)];

		if(world.getBlockState(pos.offset(dir)).getBlock() == Blocks.AIR) {
			world.setBlockState(pos.offset(dir), getGas().getDefaultState());
		}
	}

	@Override
	public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {

		if(onBreak) {
			world.setBlockState(pos, this.getGas().getDefaultState());
		}

		super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);
	}

	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {

		if(onNeighbour && world instanceof World) {

			World w = (World) world;
			for(EnumFacing dir : EnumFacing.values()) {

				if(w.getBlockState(pos.offset(dir)).getBlock() == Blocks.AIR) {
					w.setBlockState(pos.offset(dir), getGas().getDefaultState());
				}
			}
		}
	}
}
