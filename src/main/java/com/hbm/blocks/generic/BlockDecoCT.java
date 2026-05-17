package com.hbm.blocks.generic;

import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

public class BlockDecoCT extends Block {

    public boolean allowFortune = true;

    public BlockDecoCT(Material mat) {
        super(mat);
    }

    public BlockDecoCT noFortune() {
        this.allowFortune = false;
        return this;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        if (fortune > 0 && allowFortune
                && Item.getItemFromBlock(this) != this.getItemDropped(this.getDefaultState(), rand, fortune)) {
            int mult = rand.nextInt(fortune + 2) - 1;
            if (mult < 0) mult = 0;
            return this.quantityDropped(rand) * (mult + 1);
        } else {
            return this.quantityDropped(rand);
        }
    }
}
