package com.hbm.items.blocks;

import com.hbm.blocks.IBlockMulti;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemBlockEnumMulti extends ItemBlock {

    public ItemBlockEnumMulti(Block block) {
        super(block);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        IBlockMulti multi = (IBlockMulti) this.block;
        return super.getUnlocalizedName(stack) + "." + (stack.getItemDamage() % multi.getSubCount());
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        IBlockMulti multi = (IBlockMulti) this.block;
        for (int i = 0; i < multi.getSubCount(); i++) {
            items.add(new ItemStack(this, 1, i));
        }
    }
}
