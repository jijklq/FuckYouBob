package com.hbm.blocks.generic;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.Random;

//~ stealth-todo: Bob's ctor (String flatFace, String rim, Material, boolean) — 1.12.2 BlockPane uses (Material, boolean); textures via blockstate JSON, String params unused
//~ context: pane connects to BlockNTMGlass family via canPaneConnectTo override (1.12.2 = IBlockAccess+BlockPos+EnumFacing, 1.7.10 = IBlockAccess+x+y+z+ForgeDirection); pane geometry render-deferred (cube_all blockstate fallback)
// Bob's: this.opaque = true — field removed in 1.12.2; BlockPane.isOpaqueCube already false
// Bob's: getRenderBlockPass() → getBlockLayer() — BlockPane default inherited, no override needed
public class BlockNTMGlassPane extends BlockPane {

    final int renderLayer;
    boolean doesDrop = false;

    public BlockNTMGlassPane(int layer, String name, String rimTextureName, Material material, boolean doesDrop) {
        super(material, false);
        this.renderLayer = layer;
        this.doesDrop = doesDrop;
        this.setLightOpacity(1);
        //~ stealth-todo: Bob's this.opaque = true — 1.12.2 no opaque field; review isOpaqueCube at render phase if needed
    }

    // 1.12.2 BlockPane connection API: canPaneConnectTo(IBlockAccess, BlockPos, EnumFacing)
    // pos is the adjacent block's position, dir is the facing toward it
    // Bob's 1.7.10: canPaneConnectTo(IBlockAccess, int x, int y, int z, ForgeDirection)
    @Override
    public boolean canPaneConnectTo(IBlockAccess world, BlockPos pos, EnumFacing dir) {
        Block block = world.getBlockState(pos).getBlock();
        return super.canPaneConnectTo(world, pos, dir) || block instanceof BlockNTMGlass;
    }

    @Override
    public int quantityDropped(Random rand) {
        return doesDrop ? 1 : 0;
    }
}
