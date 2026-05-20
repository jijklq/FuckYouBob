package com.ntmtest.sub;

import com.ntmtest.CmdRoot;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class CmdEntities implements CmdRoot.SubCmd {

    @Override
    public void run(MinecraftServer server, ICommandSender sender, String[] args) throws Exception {
        if (args.length < 1) {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] error=usage:/ntmtest entities <radius>"));
            return;
        }
        double r     = CommandBase.parseDouble(args[0]);
        World  world = sender.getEntityWorld();

        AxisAlignedBB box = new AxisAlignedBB(
            sender.getPosition().getX() - r, sender.getPosition().getY() - r, sender.getPosition().getZ() - r,
            sender.getPosition().getX() + r, sender.getPosition().getY() + r, sender.getPosition().getZ() + r);

        List<Entity> ents = world.getEntitiesWithinAABB(Entity.class, box);
        sender.sendMessage(new TextComponentString("[NTMTEST] entities_count=" + ents.size()));

        for (Entity ent : ents) {
            NBTTagCompound nbt = new NBTTagCompound();
            try { ent.writeToNBT(nbt); } catch (Exception e) { /* best-effort */ }
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] entity id=" + ent.getEntityId() +
                " type=" + ent.getName() +
                " pos=" + ent.posX + "," + ent.posY + "," + ent.posZ +
                " nbt=" + nbt));
        }
    }
}
