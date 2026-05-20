package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ISpotlight;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.Spotlight;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TritiumLamp extends Block implements ISpotlight {

	private final boolean isOn;

	//~ context: setSoundType moved into constructor — protected in 1.12.2 vs public chain in 1.7.10
	public TritiumLamp(Material mat, boolean isOn) {
		super(mat);
		this.setSoundType(SoundType.GLASS);
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

			updateBeam(world, pos);
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

			updateBeam(world, pos);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

		if(!world.isRemote && this.isOn && !world.isBlockPowered(pos)) {
			world.setBlockState(pos, getOff().getDefaultState(), 2);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		super.breakBlock(world, pos, state);
		if(world.isRemote) return;

		for(EnumFacing dir : EnumFacing.VALUES) Spotlight.unpropagateBeam(world, pos, dir);
	}

	private void updateBeam(World world, BlockPos pos) {
		if(!isOn) return;

		for(EnumFacing dir : EnumFacing.VALUES) Spotlight.propagateBeam(world, pos, dir, getBeamLength(), getMeta());
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Item.getItemFromBlock(getOff());
	}

	protected int getMeta() {
		if(this == ModBlocks.lamp_tritium_green_off || this == ModBlocks.lamp_tritium_green_on) return Spotlight.META_GREEN;
		if(this == ModBlocks.lamp_tritium_blue_off || this == ModBlocks.lamp_tritium_blue_on) return Spotlight.META_BLUE;
		return Spotlight.META_YELLOW;
	}

	protected Block getOff() {
		if(this == ModBlocks.lamp_tritium_green_on) return ModBlocks.lamp_tritium_green_off;
		if(this == ModBlocks.lamp_tritium_blue_on) return ModBlocks.lamp_tritium_blue_off;
		return this;
	}

	protected Block getOn() {
		if(this == ModBlocks.lamp_tritium_green_off) return ModBlocks.lamp_tritium_green_on;
		if(this == ModBlocks.lamp_tritium_blue_off) return ModBlocks.lamp_tritium_blue_on;
		return this;
	}

	@Override
	public int getBeamLength() {
		return 8;
	}
}
