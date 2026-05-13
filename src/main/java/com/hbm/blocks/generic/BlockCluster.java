package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

import java.util.Random;

public class BlockCluster extends BlockOre {

	public BlockCluster(Material mat) {
		super(mat);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if(this == ModBlocks.cluster_iron)           return ModItems.crystal_iron;
		if(this == ModBlocks.cluster_titanium)       return ModItems.crystal_titanium;
		if(this == ModBlocks.cluster_aluminium)      return ModItems.crystal_aluminium;
		if(this == ModBlocks.cluster_copper)         return ModItems.crystal_copper;
		if(this == ModBlocks.cluster_depth_iron)     return ModItems.crystal_iron;
		if(this == ModBlocks.cluster_depth_titanium) return ModItems.crystal_titanium;
		if(this == ModBlocks.cluster_depth_tungsten) return ModItems.crystal_tungsten;
		return Item.getItemFromBlock(this);
	}
}
