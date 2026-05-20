package com.ntmtest;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Ntmtest.MODID, name = "NTM Test Helper", version = "1.0.0", acceptableRemoteVersions = "*")
public class Ntmtest {
    public static final String MODID = "ntmtest";

    @Mod.EventHandler
    public void onServerStart(FMLServerStartingEvent event) {
        event.registerServerCommand(new CmdRoot());
    }
}
