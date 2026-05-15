package com.hbm.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;

import java.util.Locale;

public abstract class BlockEnumMulti extends BlockBase implements IBlockMulti {

    public Class<? extends Enum> theEnum;
    public boolean multiName;

    public BlockEnumMulti(Material mat, Class<? extends Enum> theEnum, boolean multiName) {
        super(mat);
        this.theEnum = theEnum;
        this.multiName = multiName;
        this.setDefaultState(this.blockState.getBaseState().withProperty(getVariantProperty(), 0));
    }

    protected abstract PropertyInteger getVariantProperty();

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getVariantProperty());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(getVariantProperty(), rectify(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(getVariantProperty());
    }

    @Override
    public int getSubCount() {
        return this.theEnum.getEnumConstants().length;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        if (this.multiName) {
            Enum num = theEnum.getEnumConstants()[rectify(stack.getMetadata())];
            return getUnlocalizedMultiName(num);
        }
        return this.getUnlocalizedName();
    }

    public String getUnlocalizedMultiName(Enum num) {
        return this.getUnlocalizedName() + "." + num.name().toLowerCase(Locale.US);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return rectify(state.getValue(getVariantProperty()));
    }

}
