package com.hbm.extprop;

import net.minecraft.entity.player.EntityPlayer;

//~ stealth-todo: full HbmPlayerProps system (Forge Capability in 1.12.2 / IExtendedEntityProperties in 1.7.10) deferred to player-prop subsystem phase
//~ context: stub provides transient isOnLadder field for BlockWoodStructure SCAFFOLD climb; full subsystem on separate phase
public class HbmPlayerProps {

    public boolean isOnLadder = false;

    public static HbmPlayerProps getData(EntityPlayer player) {
        // STUB: returns transient instance — no actual capability storage.
        // SCAFFOLD climb won't function until full subsystem ported (Capability + per-player persistence).
        return new HbmPlayerProps();
    }
}
