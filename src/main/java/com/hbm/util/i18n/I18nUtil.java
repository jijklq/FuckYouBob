package com.hbm.util.i18n;

import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class I18nUtil {

	@SideOnly(Side.CLIENT)
	public static String[] resolveKeyArray(String s, Object... args) {
		String translated = I18n.format(s, args);
		return translated.split("\n");
	}

	@SideOnly(Side.CLIENT)
	public static String resolveKey(String s, Object... args) {
		return I18n.format(s, args);
	}
}
