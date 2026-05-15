package com.hbm.blocks;

import java.util.Random;

import com.hbm.items.ModItems;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockCoral extends BlockEnumMulti {

    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 4);

    public enum EnumCoral {
        TUBE, BRAIN, BUBBLE, FIRE, HORN;
    }

    public BlockCoral() {
        super(Material.GLASS, EnumCoral.class, false);
    }

    @Override
    protected PropertyInteger getVariantProperty() {
        return VARIANT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        return super.canPlaceBlockAt(worldIn, pos) && worldIn.getBlockState(pos.up()).getMaterial().isLiquid();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.powder_calcium;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return 0;
    }
}
