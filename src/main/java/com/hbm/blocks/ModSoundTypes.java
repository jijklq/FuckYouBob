package com.hbm.blocks;

import net.minecraft.block.SoundType;

/**
 * Stub for HBM custom sound types — original had ModSoundType-based custom step/dig/place sounds
 * with pitch envelopes (e.g. "hbm:step.metalBlock" for grate, "hbm:block.pipePlaced" for pipe).
 * Defer real custom sounds to render/sound phase; for now route everything to vanilla SoundType.
 */
public class ModSoundTypes {
    public static final SoundType grate = SoundType.METAL;
    public static final SoundType pipe = SoundType.METAL;
    public static final SoundType mork = SoundType.STONE;
}
