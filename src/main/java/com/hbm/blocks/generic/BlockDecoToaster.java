package com.hbm.blocks.generic;

import com.hbm.blocks.BlockMulti;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockDecoToaster extends BlockMulti {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 2);
    public static final int SUB_COUNT = 3;

    protected static final AxisAlignedBB AABB_NS = new AxisAlignedBB(0.25D, 0.0D, 0.375D, 0.75D, 0.325D, 0.625D);
    protected static final AxisAlignedBB AABB_EW = new AxisAlignedBB(0.375D, 0.0D, 0.25D, 0.625D, 0.325D, 0.75D);

    public BlockDecoToaster(Material mat) {
        super(mat);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(VARIANT, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int facingBits = meta & 3;
        int variantBits = ((meta >> 2) & 3) % SUB_COUNT;
        EnumFacing facing;
        switch (facingBits) {
            case 0:  facing = EnumFacing.NORTH; break;
            case 1:  facing = EnumFacing.SOUTH; break;
            case 2:  facing = EnumFacing.WEST;  break;
            default: facing = EnumFacing.EAST;  break;
        }
        return this.getDefaultState()
                .withProperty(FACING, facing)
                .withProperty(VARIANT, variantBits);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int facingBits;
        switch (state.getValue(FACING)) {
            case NORTH: facingBits = 0; break;
            case SOUTH: facingBits = 1; break;
            case WEST:  facingBits = 2; break;
            default:    facingBits = 3; break;
        }
        return (state.getValue(VARIANT) << 2) | facingBits;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState()
                .withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(VARIANT, meta);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        return (facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH) ? AABB_NS : AABB_EW;
    }

    @Override
    public int getSubCount() { return SUB_COUNT; }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }
}
