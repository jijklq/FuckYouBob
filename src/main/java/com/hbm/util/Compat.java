package com.hbm.util;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

public class Compat {

	public static final String MOD_EF = "etfuturum";

	public static Block tryLoadBlock(String domain, String name) {
		return Block.REGISTRY.getObject(new ResourceLocation(domain, name));
	}
}
