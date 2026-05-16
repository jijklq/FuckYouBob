package com.hbm.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockMulti extends BlockBase implements IBlockMulti {

    public BlockMulti() { super(); }
    public BlockMulti(Material mat) { super(mat); }

    @Override
    public int damageDropped(IBlockState state) { return rectify(getMetaFromState(state)); }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> list) {
        for(int i = 0; i < getSubCount(); ++i) list.add(new ItemStack(Item.getItemFromBlock(this), 1, i));
    }
}
