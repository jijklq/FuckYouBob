package com.hbm.blocks.generic;

import com.hbm.lib.Library;
import com.hbm.render.block.ISBRHUniversal;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

//~ stealth-todo: full custom render (renderInventoryBlock + multi-piece renderWorldBlock ~80 lines) deferred to render phase;
//~ context: PropertyInteger META 0..5 maps to Library ordinals matching Bob's ForgeDirection ordinals (DOWN=0/UP=1/NORTH=2/SOUTH=3/WEST=4/EAST=5);
//~ context: getStateForPlacement applies Bob's yaw-mapping from onBlockPlacedBy (i=0→meta=2/NEG_Z, i=1→meta=5/POS_X, i=2→meta=3/POS_Z, i=3→meta=4/NEG_X)
public class BlockBarrier extends Block implements ISBRHUniversal {

    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 5);

    public BlockBarrier(Material mat) {
        super(mat);
        this.setSoundType(SoundType.WOOD);
        this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(META, MathHelper.clamp(meta, 0, 5));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    // Bob's yaw-mapping from onBlockPlacedBy:
    // i = floor(rotationYaw * 4 / 360 + 0.5) & 3
    // i=0 → meta=2 (NEG_Z/NORTH), i=1 → meta=5 (POS_X/EAST), i=2 → meta=3 (POS_Z/SOUTH), i=3 → meta=4 (NEG_X/WEST)
    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        int i = MathHelper.floor(placer.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        int m;
        if (i == 0) m = 2;
        else if (i == 1) m = 5;
        else if (i == 2) m = 3;
        else m = 4;
        return this.getDefaultState().withProperty(META, m);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        int meta = state.getValue(META);
        if (meta == Library.POS_X.ordinal()) return new AxisAlignedBB(0, 0, 0, 0.125F, 1, 1);
        if (meta == Library.POS_Z.ordinal()) return new AxisAlignedBB(0, 0, 0, 1, 1, 0.125F);
        if (meta == Library.NEG_X.ordinal()) return new AxisAlignedBB(0.875F, 0, 0, 1, 1, 1);
        if (meta == Library.NEG_Z.ordinal()) return new AxisAlignedBB(0, 0, 0.875F, 1, 1, 1);
        return FULL_BLOCK_AABB;
    }
    //~ post-port: full multi-piece collision through neighbour-aware addCollisionBoxToList — Bob has ~30 lines logic
}
