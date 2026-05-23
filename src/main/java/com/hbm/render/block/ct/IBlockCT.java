package com.hbm.render.block.ct;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

//~ stealth-todo: full IBlockCT connected-texture render system — interface methods stub'нуты, full impl deferred to render phase
//~ context: BlockNTMGlassCT uses for CT-shader render; block-only port doesn't invoke these methods
// Bob's оригинал: getFragments(IBlockAccess, int x, int y, int z) : IIcon[]
//                 canConnect(IBlockAccess, int x, int y, int z, Block) : boolean
//                 primeReceiver(IIconRegister, String, IIcon) static — убран (IIconRegister/IIcon нет в 1.12.2)
public interface IBlockCT {

    default TextureAtlasSprite[] getFragments(IBlockAccess world, BlockPos pos) {
        return null;
    }

    default boolean canConnect(IBlockAccess world, BlockPos pos, Block block) {
        return this == block;
    }
}
