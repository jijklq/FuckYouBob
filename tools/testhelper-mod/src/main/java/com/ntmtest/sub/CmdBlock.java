package com.ntmtest.sub;

import com.ntmtest.CmdRoot;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class CmdBlock implements CmdRoot.SubCmd {

    @Override
    public void run(MinecraftServer server, ICommandSender sender, String[] args) throws Exception {
        if (args.length < 3) {
            sender.sendMessage(new TextComponentString(
                "[NTMTEST] error=usage:/ntmtest block <x> <y> <z>"));
            return;
        }
        int x = CommandBase.parseInt(args[0]);
        int y = CommandBase.parseInt(args[1]);
        int z = CommandBase.parseInt(args[2]);
        BlockPos pos   = new BlockPos(x, y, z);
        World    world = sender.getEntityWorld();

        IBlockState state = world.getBlockState(pos);
        Block       block = state.getBlock();
        int         meta  = block.getMetaFromState(state);

        // IBlockState properties → {key=value,...}
        StringBuilder props = new StringBuilder("{");
        boolean first = true;
        for (IProperty<?> prop : state.getPropertyKeys()) {
            if (!first) props.append(",");
            props.append(prop.getName()).append("=").append(state.getValue(prop));
            first = false;
        }
        props.append("}");

        // AABB — guard against null (some blocks return NULL_AABB for air)
        AxisAlignedBB aabb = state.getBoundingBox(world, pos);
        String aabbStr = aabb == null
            ? "null"
            : aabb.minX + "," + aabb.minY + "," + aabb.minZ + "," +
              aabb.maxX + "," + aabb.maxY + "," + aabb.maxZ;

        sender.sendMessage(new TextComponentString(
            "[NTMTEST] block=" + String.valueOf(block.getRegistryName())));
        sender.sendMessage(new TextComponentString("[NTMTEST] meta=" + meta));
        sender.sendMessage(new TextComponentString("[NTMTEST] props=" + props));
        sender.sendMessage(new TextComponentString("[NTMTEST] aabb=" + aabbStr));
        sender.sendMessage(new TextComponentString(
            "[NTMTEST] hardness=" + block.getBlockHardness(state, world, pos)));
        sender.sendMessage(new TextComponentString(
            "[NTMTEST] light=" + state.getLightValue(world, pos)));
        sender.sendMessage(new TextComponentString(
            "[NTMTEST] opaque=" + state.isOpaqueCube()));

        TileEntity te = world.getTileEntity(pos);
        if (te == null) {
            sender.sendMessage(new TextComponentString("[NTMTEST] te_nbt=null"));
        } else {
            NBTTagCompound nbt = te.writeToNBT(new NBTTagCompound());
            sender.sendMessage(new TextComponentString("[NTMTEST] te_nbt=" + nbt.toString()));
        }
    }
}
