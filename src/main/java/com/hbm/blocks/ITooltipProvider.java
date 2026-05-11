package com.hbm.blocks;

import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.List;

public interface ITooltipProvider {

	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag);

	@SideOnly(Side.CLIENT)
	public default void addStandardInfo(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
			for(String s : I18nUtil.resolveKeyArray(((Block)this).getUnlocalizedName() + ".desc")) list.add(TextFormatting.YELLOW + s);
		} else {
			list.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Hold <" +
					TextFormatting.YELLOW + "" + TextFormatting.ITALIC + "LSHIFT" +
					TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "> to display more info");
		}
	}

	public default EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.COMMON;
	}
}
