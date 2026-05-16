package com.hbm.blocks.generic;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockHotHazard extends BlockHazard {

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);

        if(world.isRainingAt(pos.up())) {
            float ox = rand.nextFloat();
            float oz = rand.nextFloat();
            world.spawnParticle(EnumParticleTypes.CLOUD,
                    pos.getX() + ox, pos.getY() + 1, pos.getZ() + oz,
                    0.0D, 0.0D, 0.0D);
        }

        for(EnumFacing dir : EnumFacing.VALUES) {
            if(dir == EnumFacing.DOWN) continue;
            BlockPos neighbor = pos.offset(dir);
            if(world.getBlockState(neighbor).getMaterial() == Material.WATER) {
                double ix = pos.getX() + 0.5 + dir.getFrontOffsetX() + rand.nextDouble() - 0.5;
                double iy = pos.getY() + 0.5 + dir.getFrontOffsetY() + rand.nextDouble() - 0.5;
                double iz = pos.getZ() + 0.5 + dir.getFrontOffsetZ() + rand.nextDouble() - 0.5;
                if(dir.getFrontOffsetX() != 0) ix = pos.getX() + 0.5 + dir.getFrontOffsetX() * 0.5 + rand.nextDouble() * 0.125 * dir.getFrontOffsetX();
                if(dir.getFrontOffsetY() != 0) iy = pos.getY() + 0.5 + dir.getFrontOffsetY() * 0.5 + rand.nextDouble() * 0.125 * dir.getFrontOffsetY();
                if(dir.getFrontOffsetZ() != 0) iz = pos.getZ() + 0.5 + dir.getFrontOffsetZ() * 0.5 + rand.nextDouble() * 0.125 * dir.getFrontOffsetZ();
                world.spawnParticle(EnumParticleTypes.CLOUD, ix, iy, iz, 0.0, 0.0, 0.0);
            }
        }
    }
}
