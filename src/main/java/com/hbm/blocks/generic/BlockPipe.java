package com.hbm.blocks.generic;

import java.util.List;
import com.hbm.blocks.ITooltipProvider;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockPipe extends Block implements ITooltipProvider {

    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class);

    public String sideString;  // texture key, preserved for future render
    public int rType;           // pipe variant: 0=plain, 1=rim, 2=quad, 3=framed (deferred to render)

    public BlockPipe(Material mat, SoundType sound, String tex, int rType) {
        super(mat);
        this.setSoundType(sound);
        this.sideString = tex;
        this.rType = rType;
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.Y));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        // 1.7.10 meta layout: bits 4/8 encoded axis (0=Y, 4=X, 8=Z). Preserve compat.
        int axisBits = meta & 12;
        EnumFacing.Axis axis;
        if(axisBits == 4) axis = EnumFacing.Axis.X;
        else if(axisBits == 8) axis = EnumFacing.Axis.Z;
        else axis = EnumFacing.Axis.Y;
        return this.getDefaultState().withProperty(AXIS, axis);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing.Axis axis = state.getValue(AXIS);
        if(axis == EnumFacing.Axis.X) return 4;
        if(axis == EnumFacing.Axis.Z) return 8;
        return 0;  // Y
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing,
            float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(AXIS, facing.getAxis());
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) { return false; }

    @Override
    public boolean isFullCube(IBlockState state) { return false; }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add("Purely decorative");
    }
}
