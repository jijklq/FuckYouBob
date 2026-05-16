package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

public class BlockSellafieldOre extends BlockSellafieldSlaked implements IBlockMultiPass {

    private final Random rand = new Random();

    public BlockSellafieldOre(Material mat) {
        super(mat);
    }

    @Override
    public int getPasses() {
        return 2;
    }

    @Override
    public boolean shouldRenderItemMulti() {
        return true;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if(this == ModBlocks.ore_sellafield_diamond) return Items.DIAMOND;
        if(this == ModBlocks.ore_sellafield_emerald) return Items.EMERALD;
        if(this == ModBlocks.ore_sellafield_radgem) return ModItems.gem_rad;
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random rand) {
        return 1;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        if(fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.getDefaultState(), rand, fortune)) {
            int j = rand.nextInt(fortune + 2) - 1;
            if(j < 0) j = 0;
            return this.quantityDropped(rand) * (j + 1);
        } else {
            return this.quantityDropped(rand);
        }
    }

    @Override
    public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune) {
        if(this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)) {
            int j1 = 0;
            if(this == ModBlocks.ore_sellafield_diamond) j1 = MathHelper.getInt(rand, 3, 7);
            if(this == ModBlocks.ore_sellafield_emerald) j1 = MathHelper.getInt(rand, 3, 7);
            if(this == ModBlocks.ore_sellafield_radgem) j1 = MathHelper.getInt(rand, 3, 7);
            return j1;
        }
        return 0;
    }
}
