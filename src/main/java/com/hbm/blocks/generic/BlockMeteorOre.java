package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.BlockEnums;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;

public class BlockMeteorOre extends BlockEnumMulti {

    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 4);

    public BlockMeteorOre() {
        super(Material.ROCK, BlockEnums.EnumMeteorType.class, true);
    }

    @Override
    protected PropertyInteger getVariantProperty() { return VARIANT; }
}
