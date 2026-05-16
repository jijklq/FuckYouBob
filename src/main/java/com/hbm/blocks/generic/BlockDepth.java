package com.hbm.blocks.generic;

import api.hbm.item.IDepthRockTool;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockDepth extends Block implements ITooltipProvider {

    public BlockDepth() {
        super(Material.ROCK);
        this.setBlockUnbreakable();
        this.setResistance(10.0F);
    }

    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
        ItemStack stack = player.getHeldItemMainhand();
        if(!stack.isEmpty() && stack.getItem() instanceof IDepthRockTool) {
            if(((IDepthRockTool)stack.getItem()).canBreakRock(world, player, stack, this, pos))
                return (float) (1D / 50D);
        }
        return super.getPlayerRelativeBlockHardness(state, player, world, pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, net.minecraft.client.util.ITooltipFlag flag) {
        list.add(TextFormatting.YELLOW + I18nUtil.resolveKey("trait.tile.depth"));
    }
}
