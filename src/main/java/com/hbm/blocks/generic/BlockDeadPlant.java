package com.hbm.blocks.generic;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

// Stub — full port deferred; enum variants needed by BlockNTMFlower and BlockTallPlant
public class BlockDeadPlant extends Block {

    public enum EnumDeadPlantType {
        GENERIC, BIGFLOWER;
    }

    public BlockDeadPlant() {
        super(Material.PLANTS);
    }
}
