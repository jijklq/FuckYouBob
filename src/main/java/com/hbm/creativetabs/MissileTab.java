package com.hbm.creativetabs;

import com.hbm.items.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class MissileTab extends CreativeTabs {

    public MissileTab(String label) {
        super(label);
    }

    @Override
    public ItemStack getTabIconItem() {
        if (ModItems.missile_nuclear != null)
            return new ItemStack(ModItems.missile_nuclear);
        return new ItemStack(Items.IRON_PICKAXE);
    }
}
