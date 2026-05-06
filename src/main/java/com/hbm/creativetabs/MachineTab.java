package com.hbm.creativetabs;

import com.hbm.blocks.ModBlocks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class MachineTab extends CreativeTabs {

    public MachineTab(String label) {
        super(label);
    }

    @Override
    public ItemStack getTabIconItem() {
        if (ModBlocks.pwr_controller != null)
            return new ItemStack(Item.getItemFromBlock(ModBlocks.pwr_controller));
        return new ItemStack(Items.IRON_PICKAXE);
    }
}
