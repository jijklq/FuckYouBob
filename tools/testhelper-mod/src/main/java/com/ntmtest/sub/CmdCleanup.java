package com.ntmtest.sub;

import com.ntmtest.CmdRoot;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class CmdCleanup implements CmdRoot.SubCmd {

    @Override
    public void run(MinecraftServer server, ICommandSender sender, String[] args) throws Exception {
        if (args.length < 6) {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] error=usage:/ntmtest cleanup <x1> <y1> <z1> <x2> <y2> <z2>"));
            return;
        }
        int ax1 = CommandBase.parseInt(args[0]);
        int ay1 = CommandBase.parseInt(args[1]);
        int az1 = CommandBase.parseInt(args[2]);
        int ax2 = CommandBase.parseInt(args[3]);
        int ay2 = CommandBase.parseInt(args[4]);
        int az2 = CommandBase.parseInt(args[5]);

        int x1 = Math.min(ax1, ax2),  x2 = Math.max(ax1, ax2);
        int y1 = Math.min(ay1, ay2),  y2 = Math.max(ay1, ay2);
        int z1 = Math.min(az1, az2),  z2 = Math.max(az1, az2);

        World world     = sender.getEntityWorld();
        int   blocksSet = 0;

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
                    blocksSet++;
                }
            }
        }

        AxisAlignedBB box = new AxisAlignedBB(x1, y1, z1, x2 + 1, y2 + 1, z2 + 1);
        List<Entity> ents = world.getEntitiesWithinAABB(Entity.class, box);
        int killed = 0;
        for (Entity e : ents) {
            if (e instanceof EntityPlayer) continue;
            e.setDead();
            killed++;
        }

        sender.sendMessage(new TextComponentString(
            "[NTMTEST] cleanup_ok blocks=" + blocksSet + " entities=" + killed));
    }
}
