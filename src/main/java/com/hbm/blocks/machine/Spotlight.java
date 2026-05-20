package com.hbm.blocks.machine;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

//~ stealth-todo: replace stub with full Bob's Spotlight (350+ lines, TileEntity machine) at TE phase
//~ context: TritiumLamp depends only on META_* constants + 2 static methods; stub allows block-only port
public class Spotlight extends Block {

	public static final int META_YELLOW = 0;
	public static final int META_GREEN = 1;
	public static final int META_BLUE = 2;

	public Spotlight() {
		super(Material.IRON);
	}

	public static void propagateBeam(World world, BlockPos pos, EnumFacing dir, int distance, int meta) {
		// no-op stub — full beam propagation deferred to TE phase
	}

	public static void unpropagateBeam(World world, BlockPos pos, EnumFacing dir) {
		// no-op stub — full beam cleanup deferred to TE phase
	}
}
