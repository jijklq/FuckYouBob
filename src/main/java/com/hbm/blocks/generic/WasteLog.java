package com.hbm.blocks.generic;

import java.util.Random;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class WasteLog extends Block {

    private static final Random RANDOM = new Random();

    public WasteLog(Material mat) {
        super(mat);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == ModBlocks.waste_log) return Items.COAL;
        if (this == ModBlocks.frozen_log) return Items.SNOWBALL;
        return Items.AIR;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (this == ModBlocks.waste_log && RANDOM.nextInt(1000) == 0) {
            drops.add(new ItemStack(ModItems.burnt_bark));
            return;
        }
        super.getDrops(drops, world, pos, state, fortune);
    }

    @Override
    public int quantityDropped(Random rand) {
        return 2 + rand.nextInt(3);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 1;
    }
}
