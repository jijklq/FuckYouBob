package com.hbm.blocks.generic;

import com.hbm.blocks.IStepTickReceiver;
import com.hbm.blocks.ITooltipProvider;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class BlockSpeedy extends Block implements IStepTickReceiver, ITooltipProvider {

    final double speed;

    public BlockSpeedy(Material mat, double speed) {
        super(mat);
        this.speed = speed;
    }

    //~ stealth-todo: Bob invokes onPlayerStep via PlayerTickHandler batch (not ported);
    //~ context: 1.12.2-native vanilla hook onEntityWalk used instead, equivalent client-side behavior
    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (!world.isRemote) return;
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) entity;
        if (player.moveForward != 0 || player.moveStrafing != 0) {
            entity.motionX *= speed;
            entity.motionZ *= speed;
        }
    }

    @Override
    public void onPlayerStep(World world, BlockPos pos, EntityPlayer player) {
        // IStepTickReceiver promise kept; same logic as onEntityWalk
        if (!world.isRemote) return;
        if (player.moveForward != 0 || player.moveStrafing != 0) {
            player.motionX *= speed;
            player.motionZ *= speed;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add(TextFormatting.BLUE + "Increases speed by " + MathHelper.floor((speed - 1) * 100) + "%");
    }
}
