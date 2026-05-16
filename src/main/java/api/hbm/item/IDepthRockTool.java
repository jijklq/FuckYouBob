package api.hbm.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IDepthRockTool {
    boolean canBreakRock(World world, EntityPlayer player, ItemStack tool, Block block, BlockPos pos);
}
