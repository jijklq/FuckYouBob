package com.ntmtest.sub;

import com.ntmtest.CmdRoot;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CmdCanSpawn implements CmdRoot.SubCmd {

    @Override
    public void run(MinecraftServer server, ICommandSender sender, String[] args) throws Exception {
        if (args.length < 4) {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] error=usage:/ntmtest canspawn <entity_id> <x> <y> <z>"));
            return;
        }
        ResourceLocation rl = new ResourceLocation(args[0]);
        int x = CommandBase.parseInt(args[1]);
        int y = CommandBase.parseInt(args[2]);
        int z = CommandBase.parseInt(args[3]);

        World  world = sender.getEntityWorld();
        Entity ent   = EntityList.createEntityByIDFromName(rl, world);

        if (ent == null) {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] canspawn=false reason=unknown_entity:" + rl));
            return;
        }

        ent.setPosition(x + 0.5, y, z + 0.5);

        boolean canSpawn;
        String  reason;
        if (ent instanceof EntityLiving) {
            EntityLiving liv = (EntityLiving) ent;
            canSpawn = liv.getCanSpawnHere() && liv.isNotColliding();
            reason   = canSpawn ? "ok" : "spawn_check_failed";
        } else {
            canSpawn = false;
            reason   = "not_living_entity";
        }
        sender.sendMessage(new TextComponentString(
            "[NTMTEST] canspawn=" + canSpawn + " reason=" + reason));
    }
}
