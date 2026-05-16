package com.hbm.blocks.generic;

import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockNTMGlass extends BlockBreakable {
    int renderLayer;
    boolean doesDrop = false;

    public BlockNTMGlass(int layer, Material material) {
        this(layer, material, false);
    }

    public BlockNTMGlass(int layer, Material material, boolean doesDrop) {
        super(material, false);
        this.renderLayer = layer;
        this.doesDrop = doesDrop;
    }

    @Override
    public int quantityDropped(Random rand) { return doesDrop ? 1 : 0; }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return renderLayer == 0 ? BlockRenderLayer.CUTOUT : BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    protected boolean canSilkHarvest() { return true; }
}
