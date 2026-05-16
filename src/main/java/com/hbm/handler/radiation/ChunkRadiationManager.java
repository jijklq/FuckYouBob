package com.hbm.handler.radiation;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChunkRadiationManager {
    public static ChunkRadiationManager proxy = new ChunkRadiationManager();

    public void incrementRad(World world, BlockPos pos, float amount) {
        // TODO: реальная радиация на radiation-этапе
    }
}
