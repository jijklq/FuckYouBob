package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ReinforcedLamp extends Block {

	private final boolean isOn;

	public ReinforcedLamp(Material mat, boolean isOn) {
		super(mat);
		this.isOn = isOn;

		if(isOn) {
			this.setLightLevel(1.0F);
		}
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {

		if(!world.isRemote) {

			if(this.isOn && !world.isBlockPowered(pos)) {
				world.scheduleUpdate(pos, this, 4);

			} else if(!this.isOn && world.isBlockPowered(pos)) {
				world.setBlockState(pos, getOn().getDefaultState(), 2);
			}
		}
	}

	@Override
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {

		if(!world.isRemote) {

			if(this.isOn && !world.isBlockPowered(pos)) {
				world.scheduleUpdate(pos, this, 4);

			} else if(!this.isOn && world.isBlockPowered(pos)) {
				world.setBlockState(pos, getOn().getDefaultState(), 2);
			}
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote && this.isOn && !world.isBlockPowered(pos)) {
			world.setBlockState(pos, getOff().getDefaultState(), 2);
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(getOff());
	}

	protected Block getOff() {
		if(this == ModBlocks.reinforced_lamp_on) return ModBlocks.reinforced_lamp_off;
		return this;
	}

	protected Block getOn() {
		if(this == ModBlocks.reinforced_lamp_off) return ModBlocks.reinforced_lamp_on;
		return this;
	}
}
