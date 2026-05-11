package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.SpaceConfig;
import com.hbm.dim.SolarSystem;
import com.hbm.items.ModItems;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BlockOre extends Block implements IBlockMultiPass, IBlockMulti, ITooltipProvider {

	public Set<SolarSystem.Body> spawnsOn = new HashSet<>();

	public BlockOre(Material mat) {
		super(mat);
	}

	public static void addValidBody(Block ore, SolarSystem.Body body) {
		if(!(ore instanceof BlockOre)) return;
		((BlockOre) ore).spawnsOn.add(body);
	}

	public static void addAllBodies(Block ore) {
		for(SolarSystem.Body celestial : SolarSystem.Body.values()) {
			addValidBody(ore, celestial);
		}
	}

	public static void addAllExcept(Block ore, SolarSystem.Body body) {
		for(SolarSystem.Body celestial : SolarSystem.Body.values()) {
			if(celestial == body) continue;
			addValidBody(ore, celestial);
		}
	}

	public static Map<Block, BlockOre> vanillaMap = new HashMap<>();

	public BlockOre(Material mat, Block vanillaBlock) {
		this(mat);
		vanillaMap.put(vanillaBlock, this);
	}

	@Override
	public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		if(this == ModBlocks.ore_oil) return false;
		if(this == ModBlocks.ore_gas) return false;
		if(this == ModBlocks.ore_brine) return false;
		if(this == ModBlocks.ore_tekto) return false;
		return super.canSilkHarvest(world, pos, state, player);
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		if(this == ModBlocks.ore_fluorite) {
			return ModItems.fluorite;
		}
		if(this == ModBlocks.ore_niter) {
			return ModItems.niter;
		}
		if(this == ModBlocks.ore_sulfur) {
			return ModItems.sulfur;
		}
		if(this == ModBlocks.ore_glowstone) {
			return Items.GLOWSTONE_DUST;
		}
		if(this == ModBlocks.ore_fire) {
			return rand != null && rand.nextInt(10) == 0 ? ModItems.ingot_phosphorus : ModItems.powder_fire;
		}
		if(this == ModBlocks.ore_rare) {
			return ModItems.chunk_ore;
		}
		if(this == ModBlocks.ore_asbestos) {
			return ModItems.ingot_asbestos;
		}
		if(this == ModBlocks.ore_lignite) {
			return ModItems.lignite;
		}
		if(this == ModBlocks.ore_cinnebar) {
			return ModItems.cinnebar;
		}
		if(this == ModBlocks.ore_coltan) {
			return ModItems.fragment_coltan;
		}
		if(this == ModBlocks.ore_cobalt) {
			return ModItems.fragment_cobalt;
		}
		// Vanilla reproduction
		if(this == ModBlocks.ore_redstone) {
			return Items.REDSTONE;
		}
		if(this == ModBlocks.ore_lapis) {
			return Items.DYE;
		}
		if(this == ModBlocks.ore_emerald) {
			return Items.EMERALD;
		}
		if(this == ModBlocks.ore_quartz) {
			return Items.QUARTZ;
		}
		if(this == ModBlocks.ore_diamond) {
			return Items.DIAMOND;
		}

		if(this == ModBlocks.ore_oil) return ModItems.oil_tar;
		if(this == ModBlocks.ore_gas) return Item.getItemFromBlock(ModBlocks.ore_gas_empty);
		if(this == ModBlocks.ore_brine) return Item.getItemFromBlock(ModBlocks.ore_brine_empty);
		if(this == ModBlocks.ore_tekto) return Item.getItemFromBlock(ModBlocks.ore_tekto_empty);

		// Sellafield ores (BlockSellafieldOre in 1.7.10 — merged into BlockOre in 1.12.2 port)
		if(this == ModBlocks.ore_sellafield_diamond) return Items.DIAMOND;
		if(this == ModBlocks.ore_sellafield_emerald) return Items.EMERALD;
		if(this == ModBlocks.ore_sellafield_radgem) return ModItems.gem_rad;

		return Item.getItemFromBlock(this);
	}

	@Override
	public int quantityDropped(Random rand) {
		if(this == ModBlocks.ore_fluorite) return 2 + rand.nextInt(3);
		if(this == ModBlocks.ore_niter) return 2 + rand.nextInt(3);
		if(this == ModBlocks.ore_sulfur ||
				this == ModBlocks.ore_nether_sulfur) return 2 + rand.nextInt(3);
		if(this == ModBlocks.block_meteor_broken) return 1 + rand.nextInt(3);
		if(this == ModBlocks.block_meteor_treasure) return 1 + rand.nextInt(3);
		if(this == ModBlocks.ore_cobalt) return 4 + rand.nextInt(6);
		if(this == ModBlocks.ore_nether_cobalt) return 5 + rand.nextInt(8);

		if(this == ModBlocks.ore_glowstone) return 1 + rand.nextInt(3);
		if(this == ModBlocks.ore_redstone) return 4 + rand.nextInt(2);
		if(this == ModBlocks.ore_lapis) return 4 + rand.nextInt(5);
		// Sellafield ores drop 3–7 items (matches original BlockSellafieldOre)
		if(this == ModBlocks.ore_sellafield_diamond) return 3 + rand.nextInt(5);
		if(this == ModBlocks.ore_sellafield_emerald) return 3 + rand.nextInt(5);
		if(this == ModBlocks.ore_sellafield_radgem) return 3 + rand.nextInt(5);
		return 1;
	}

	public boolean allowFortune = true;

	public BlockOre noFortune() {
		this.allowFortune = false;
		return this;
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, Random rand) {
		if(fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.getDefaultState(), rand, fortune) && allowFortune) {
			int mult = rand.nextInt(fortune + 2) - 1;
			return this.quantityDropped(rand) * (Math.max(mult, 0) + 1);
		} else {
			return this.quantityDropped(rand);
		}
	}

	@Override
	public int damageDropped(IBlockState state) {
		if(this == ModBlocks.ore_lapis) return 4; // Items.DYE meta 4 = lapis lazuli (meta 0 = ink sac)
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
		if(!SpaceConfig.showOreLocations) return;
		if(spawnsOn.isEmpty()) return;

		if(spawnsOn.size() == SolarSystem.Body.values().length) {
			list.add(TextFormatting.GOLD + "Can be found anywhere");
			return;
		} else if(spawnsOn.size() == SolarSystem.Body.values().length - 1) {
			list.add(TextFormatting.GOLD + "Can be found anywhere except:");
			for(SolarSystem.Body body : SolarSystem.Body.values()) {
				if(spawnsOn.contains(body)) continue;
				list.add(TextFormatting.RED + " - " + I18nUtil.resolveKey("body." + body.name));
			}
			return;
		}

		list.add(TextFormatting.GOLD + "Can be found on:");
		for(SolarSystem.Body body : spawnsOn) {
			list.add(TextFormatting.AQUA + " - " + I18nUtil.resolveKey("body." + body.name));
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		if(this == ModBlocks.ore_australium) return EnumRarity.UNCOMMON;
		if(this == ModBlocks.ore_rare) return EnumRarity.UNCOMMON;
		return EnumRarity.COMMON;
	}

	@Override
	public int getPasses() {
		return 1;
	}

	@Override
	public boolean shouldRenderItemMulti() {
		return false;
	}

	@Override
	public int getSubCount() {
		return 1;
	}
}
