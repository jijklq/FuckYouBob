package com.ntmtest.sub;

import com.ntmtest.CmdRoot;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdEffects implements CmdRoot.SubCmd {

    @Override
    public void run(MinecraftServer server, ICommandSender sender, String[] args) throws Exception {
        EntityPlayerMP player;
        if (args.length >= 1) {
            player = server.getPlayerList().getPlayerByUsername(args[0]);
            if (player == null) {
                sender.sendMessage(new TextComponentString(
                    "[NTMTEST] error=player_not_found:" + args[0]));
                return;
            }
        } else if (sender instanceof EntityPlayerMP) {
            player = (EntityPlayerMP) sender;
        } else {
            sender.sendMessage(new TextComponentString("[NTMTEST] error=specify_player"));
            return;
        }

        sender.sendMessage(new TextComponentString(
            "[NTMTEST] hp=" + player.getHealth() + "/" + player.getMaxHealth()));
        sender.sendMessage(new TextComponentString(
            "[NTMTEST] food=" + player.getFoodStats().getFoodLevel() +
            " sat=" + player.getFoodStats().getSaturationLevel()));
        sender.sendMessage(new TextComponentString(
            "[NTMTEST] air=" + player.getAir() +
            " xp=" + player.experienceLevel +
            "(" + player.experienceTotal + ")"));

        for (PotionEffect eff : player.getActivePotionEffects()) {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] effect=" + eff.getEffectName() +
                " amp=" + eff.getAmplifier() +
                " dur=" + eff.getDuration() +
                " particles=" + eff.doesShowParticles()));
        }
    }
}
