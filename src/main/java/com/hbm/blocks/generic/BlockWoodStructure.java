package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.render.block.ISBRHUniversal;
import com.hbm.util.EnumUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

//~ stealth-todo: full custom render (renderInventoryBlock + renderWorldBlock via RenderBlocksNT, ~120 lines, multi-piece geometry per variant) deferred to render phase
//~ context: ladder mechanic depends on HbmPlayerProps stub — climbing non-functional until full player-prop subsystem ported
//~ post-port: addCollisionBoxesToList multi-piece collision simplified to single getBoundingBox
public class BlockWoodStructure extends BlockEnumMulti implements ISBRHUniversal {

    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 2);

    public BlockWoodStructure(Material mat) {
        super(mat, EnumWoodStructure.class, true);
    }

    public enum EnumWoodStructure {
        ROOF, SCAFFOLD, CEILING
    }

    @Override
    protected PropertyInteger getVariantProperty() {
        return VARIANT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumWoodStructure type = EnumUtil.grabEnumSafely(EnumWoodStructure.class, getMetaFromState(state));
        if (type == EnumWoodStructure.SCAFFOLD && side == EnumFacing.UP) return true;
        return false;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        EnumWoodStructure type = EnumUtil.grabEnumSafely(EnumWoodStructure.class, getMetaFromState(state));
        if (type == EnumWoodStructure.ROOF)     return new AxisAlignedBB(0F, 0F, 0F, 1F, 0.1875F, 1F);
        if (type == EnumWoodStructure.SCAFFOLD)  return new AxisAlignedBB(0.0625F, 0F, 0.0625F, 0.9375F, 1F, 0.9375F);
        if (type == EnumWoodStructure.CEILING)  return new AxisAlignedBB(0F, 0.875F, 0F, 1F, 1F, 1F);
        return FULL_BLOCK_AABB;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EnumWoodStructure type = EnumUtil.grabEnumSafely(EnumWoodStructure.class, getMetaFromState(state));
        if (type != EnumWoodStructure.SCAFFOLD) return;
        EntityPlayer player = (EntityPlayer) entity;
        HbmPlayerProps props = HbmPlayerProps.getData(player);
        props.isOnLadder = true;
        //~ stealth-todo: ladder set has no effect until HbmPlayerProps full subsystem (Capability) ported
    }
}
