package com.ntmtest;

import com.ntmtest.sub.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CmdRoot extends CommandBase {

    private static final Logger LOG = LogManager.getLogger("ntmtest");

    private final Map<String, SubCmd> subs = new LinkedHashMap<>();

    public CmdRoot() {
        subs.put("block",    new CmdBlock());
        subs.put("item",     new CmdItem());
        subs.put("effects",  new CmdEffects());
        subs.put("canspawn", new CmdCanSpawn());
        subs.put("entities", new CmdEntities());
        subs.put("cleanup",  new CmdCleanup());
    }

    @Override @Nonnull
    public String getName() { return "ntmtest"; }

    @Override
    public int getRequiredPermissionLevel() { return 2; }

    @Override @Nonnull
    public String getUsage(@Nonnull ICommandSender sender) {
        return "/ntmtest <" + String.join("|", subs.keySet()) + "> [args]";
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender,
                        @Nonnull String[] args) throws CommandException {
        if (args.length == 0) {
            sender.sendMessage(new TextComponentString("[NTMTEST] error=usage " + getUsage(sender)));
            return;
        }
        SubCmd sub = subs.get(args[0]);
        if (sub == null) {
            sender.sendMessage(new TextComponentString("[NTMTEST] error=unknown_subcommand:" + args[0]));
            return;
        }
        String[] rest = Arrays.copyOfRange(args, 1, args.length);
        // Wrap sender so all [NTMTEST] messages are also written to server log
        ICommandSender loggingSender = new LoggingCommandSender(sender);
        try {
            sub.run(server, loggingSender, rest);
        } catch (Exception e) {
            String msg = "[NTMTEST] error=" + e.getClass().getSimpleName() + ":" + e.getMessage();
            LOG.info(msg);
            sender.sendMessage(new TextComponentString(msg));
        }
    }

    @Override @Nonnull
    public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender,
                                          @Nonnull String[] args, BlockPos targetPos) {
        if (args.length == 1) return getListOfStringsMatchingLastWord(args, subs.keySet());
        return Collections.emptyList();
    }

    /** Sub-command interface */
    public interface SubCmd {
        void run(MinecraftServer server, ICommandSender sender, String[] args) throws Exception;
    }

    /** Forwards all ICommandSender calls to the real sender; also logs sendMessage to server log. */
    private static final class LoggingCommandSender implements ICommandSender {
        private final ICommandSender delegate;
        LoggingCommandSender(ICommandSender delegate) { this.delegate = delegate; }

        @Override
        public void sendMessage(ITextComponent component) {
            String text = component.getUnformattedText();
            if (text.startsWith("[NTMTEST]")) LOG.info(text);
            delegate.sendMessage(component);
        }

        @Override @Nonnull public String getName() { return delegate.getName(); }
        @Override public boolean canUseCommand(int level, @Nonnull String cmd) { return delegate.canUseCommand(level, cmd); }
        @Override @Nonnull public BlockPos getPosition() { return delegate.getPosition(); }
        @Override @Nonnull public Vec3d getPositionVector() { return delegate.getPositionVector(); }
        @Override @Nonnull public World getEntityWorld() { return delegate.getEntityWorld(); }
        @Override @Nullable public Entity getCommandSenderEntity() { return delegate.getCommandSenderEntity(); }
        @Override public boolean sendCommandFeedback() { return delegate.sendCommandFeedback(); }
        @Override public void setCommandStat(@Nonnull CommandResultStats.Type type, int amount) { delegate.setCommandStat(type, amount); }
        @Override @Nullable public MinecraftServer getServer() { return delegate.getServer(); }
    }
}
