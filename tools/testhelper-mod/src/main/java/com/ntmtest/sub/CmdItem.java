package com.ntmtest.sub;

import com.ntmtest.CmdRoot;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CmdItem implements CmdRoot.SubCmd {

    @Override
    public void run(MinecraftServer server, ICommandSender sender, String[] args) throws Exception {
        if (!(sender instanceof EntityPlayerMP)) {
            sender.sendMessage(new TextComponentString("[NTMTEST] error=must_be_player"));
            return;
        }
        EntityPlayerMP player   = (EntityPlayerMP) sender;
        String         slotSpec = args.length > 0 ? args[0] : "hand";

        if (slotSpec.equals("dump")) {
            for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
                emit(sender, "main" + i, player.inventory.mainInventory.get(i));
            }
            for (int i = 0; i < player.inventory.armorInventory.size(); i++) {
                emit(sender, "armor" + i, player.inventory.armorInventory.get(i));
            }
            emit(sender, "offhand", player.getHeldItemOffhand());
            return;
        }

        ItemStack stack;
        if (slotSpec.equals("hand")) {
            stack = player.getHeldItemMainhand();
        } else if (slotSpec.equals("offhand")) {
            stack = player.getHeldItemOffhand();
        } else if (slotSpec.startsWith("hotbar")) {
            int idx = Integer.parseInt(slotSpec.substring("hotbar".length()));
            stack = player.inventory.mainInventory.get(idx);
        } else if (slotSpec.startsWith("main")) {
            int idx = Integer.parseInt(slotSpec.substring("main".length()));
            stack = player.inventory.mainInventory.get(idx);
        } else if (slotSpec.startsWith("armor")) {
            int idx = Integer.parseInt(slotSpec.substring("armor".length()));
            stack = player.inventory.armorInventory.get(idx);
        } else {
            sender.sendMessage(new TextComponentString("[NTMTEST] error=bad_slot:" + slotSpec));
            return;
        }
        emit(sender, slotSpec, stack);
    }

    private void emit(ICommandSender sender, String slot, ItemStack stack) {
        if (stack.isEmpty()) {
            sender.sendMessage(new TextComponentString("[NTMTEST] slot=" + slot + " item=empty"));
            return;
        }
        sender.sendMessage(new TextComponentString(
            "[NTMTEST] slot=" + slot +
            " item="    + stack.getItem().getRegistryName() +
            " count="   + stack.getCount() +
            " meta="    + stack.getMetadata() +
            " damage="  + stack.getItemDamage() +
            " display=" + stack.getDisplayName()));
        if (stack.hasTagCompound()) {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] slot=" + slot + " nbt=" + stack.getTagCompound()));
        } else {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] slot=" + slot + " nbt=null"));
        }
    }
}
