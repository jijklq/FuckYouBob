package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMulti;
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

public class BlockDecoModel extends BlockEnumMulti {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    // PropertyInteger must have range ≥ 2; only value 0 is active for single-variant blocks
    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 1);

    private float mnX = 0.0F, mnY = 0.0F, mnZ = 0.0F;
    private float mxX = 1.0F, mxY = 1.0F, mxZ = 1.0F;

    public BlockDecoModel(Material mat, Class<? extends Enum> theEnum, boolean multiName) {
        super(mat, theEnum, multiName);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(VARIANT, 0));
    }

    public BlockDecoModel setBlockBoundsTo(float minX, float minY, float minZ,
            float maxX, float maxY, float maxZ) {
        this.mnX = minX; this.mnY = minY; this.mnZ = minZ;
        this.mxX = maxX; this.mxY = maxY; this.mxZ = maxZ;
        return this;
    }

    @Override
    protected PropertyInteger getVariantProperty() { return VARIANT; }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, VARIANT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        int facingBits = meta & 3;
        int variantBits = Math.min((meta >> 2) & 3, 1);
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
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState()
                .withProperty(FACING, placer.getHorizontalFacing().getOpposite())
                .withProperty(VARIANT, Math.min(meta, 1));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        switch (state.getValue(FACING)) {
            case NORTH: return new AxisAlignedBB(1 - mxX, mnY, 1 - mxZ, 1 - mnX, mxY, 1 - mnZ);
            case SOUTH: return new AxisAlignedBB(mnX,     mnY, mnZ,      mxX,     mxY, mxZ);
            case WEST:  return new AxisAlignedBB(1 - mxZ, mnY, mnX,      1 - mnZ, mxY, mxX);
            default:    return new AxisAlignedBB(mnZ,     mnY, 1 - mxX,  mxZ,     mxY, 1 - mnX);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }
}
