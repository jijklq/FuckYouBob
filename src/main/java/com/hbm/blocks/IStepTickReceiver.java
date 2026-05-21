package com.hbm.blocks;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IStepTickReceiver {

    void onPlayerStep(World world, BlockPos pos, EntityPlayer player);
}
