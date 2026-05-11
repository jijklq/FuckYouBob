package com.hbm.handler.jei;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * JEI integration — replaces NEI's NEIConfig / NEIRegistry from 1.7.10.
 *
 * Soft dependency: this class is discovered by JEI's ASM scanner at runtime only when
 * JEI is installed. If JEI is absent the class is never loaded (no ClassNotFoundException).
 *
 * Recipe categories (machine handlers) are registered per-machine as each TileEntity
 * is ported. For now this file only handles item hiding, mirroring NEIConfig.loadConfig().
 *
 * TODOs that require further porting:
 *   - ingot_metal meta subtypes: extend when EnumIngotMetal is ported (ItemEnums.java)
 *   - item_secret / ammo_secret meta subtypes: extend when GunFactory / ItemEnums are ported
 *   - memory battery stacks: extend when ItemBattery is ported
 *   - celestial bedrock ore grades: extend when ItemBedrockOreNew is ported
 *   - polaroidID guard (book_secret, book_of_, burnt_bark, ams_core_thingy):
 *       restore conditional hide when MainRegistry.polaroidID is ported
 *   - GunFactory secret guns: extend when GunFactory is ported
 */
@JEIPlugin
public class HBMJEIPlugin implements IModPlugin {

    @Override
    public void register(IModRegistry registry) {
        IIngredientBlacklist bl = registry.getJeiHelpers().getIngredientBlacklist();
        hideItems(bl);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static void hide(IIngredientBlacklist bl, Item item) {
        if (item != null) bl.addIngredientToBlacklist(new ItemStack(item));
    }

    /** Hide all meta subtypes of a meta-item (e.g. ingot_metal). */
    private static void hideMeta(IIngredientBlacklist bl, Item item, int count) {
        if (item == null) return;
        for (int i = 0; i < count; i++) {
            bl.addIngredientToBlacklist(new ItemStack(item, 1, i));
        }
    }

    private static void hide(IIngredientBlacklist bl, Block block) {
        if (block == null) return;
        Item item = Item.getItemFromBlock(block);
        if (item != Items.AIR) bl.addIngredientToBlacklist(new ItemStack(item));
    }

    // -------------------------------------------------------------------------
    // Item hiding — translated from NEIConfig.loadConfig()
    // -------------------------------------------------------------------------

    private static void hideItems(IIngredientBlacklist bl) {

        // --- Secret items ---
        // NEI: for(Item item : ItemGunBaseNT.secrets) API.hideItem(new ItemStack(item));
        // Deferred: GunFactory not ported yet. Add here when ported.

        // NEI: for(int i = 0; i < EnumAmmoSecret.values().length; i++) API.hideItem(new ItemStack(ModItems.ammo_secret, 1, i));
        // Stub hide until EnumAmmoSecret (GunFactory) is ported:
        hide(bl, ModItems.ammo_secret);

        // NEI: for(int i = 0; i < EnumSecretType.values().length; i++) API.hideItem(new ItemStack(ModItems.item_secret, 1, i));
        // Stub hide until EnumSecretType (ItemEnums) is ported:
        hide(bl, ModItems.item_secret);

        // --- ingot_metal meta subtypes ---
        // NEI: for(int i = 0; i < EnumIngotMetal.values().length; i++) API.hideItem(new ItemStack(ModItems.ingot_metal, 1, i));
        // Stub hide until EnumIngotMetal (ItemEnums) is ported:
        hide(bl, ModItems.ingot_metal);

        // --- memory battery stacks ---
        // NEI: API.hideItem(ItemBattery.getEmptyBattery(ModItems.memory));
        // NEI: API.hideItem(ItemBattery.getFullBattery(ModItems.memory));
        // Deferred: ItemBattery not ported yet.

        // --- Machine "on" state blocks (internal state, not craftable) ---
        hide(bl, ModBlocks.machine_electric_furnace_on);
        hide(bl, ModBlocks.machine_difurnace_on);
        hide(bl, ModBlocks.machine_rtg_furnace_on);
        hide(bl, ModBlocks.reinforced_lamp_on);

        // --- Misc hidden items ---
        hide(bl, ModBlocks.statue_elb_f);
        hide(bl, ModItems.euphemium_kit);
        hide(bl, ModItems.bobmazon_hidden);
        hide(bl, ModItems.book_lore); // the broken nbt-less one shouldn't show up in normal play

        // NEI: if(MainRegistry.polaroidID != 11) { ... }
        // Always hidden until polaroidID is ported to MainRegistry:
        hide(bl, ModItems.book_secret);
        hide(bl, ModItems.book_of_);
        hide(bl, ModItems.burnt_bark);
        hide(bl, ModItems.ams_core_thingy);

        // --- Dummy blocks (pseudo-multiblock internal pieces) ---
        hide(bl, ModBlocks.dummy_block_blast);
        hide(bl, ModBlocks.dummy_port_compact_launcher);
        hide(bl, ModBlocks.dummy_port_launch_table);
        hide(bl, ModBlocks.dummy_plate_compact_launcher);
        hide(bl, ModBlocks.dummy_plate_launch_table);
        hide(bl, ModBlocks.dummy_plate_cargo);

        // --- Pink wood (unused/easter-egg) ---
        hide(bl, ModBlocks.pink_log);
        hide(bl, ModBlocks.pink_planks);
        hide(bl, ModBlocks.pink_slab);
        hide(bl, ModBlocks.pink_double_slab);
        hide(bl, ModBlocks.pink_stairs);

        // --- Spotlights (off-state internal blocks) ---
        hide(bl, ModBlocks.spotlight_incandescent_off);
        hide(bl, ModBlocks.spotlight_fluoro_off);
        hide(bl, ModBlocks.spotlight_halogen_off);
        hide(bl, ModBlocks.spotlight_beam);

        // --- Space items (not in creative) ---
        hide(bl, ModItems.rocket_custom);
        hide(bl, ModBlocks.orbital_station);

        // --- Conveyors (hidden from creative in original) ---
        hide(bl, ModBlocks.conveyor);
        hide(bl, ModBlocks.conveyor_chute);
        hide(bl, ModBlocks.conveyor_lift);
        hide(bl, ModBlocks.conveyor_express);
        hide(bl, ModBlocks.conveyor_double);
        hide(bl, ModBlocks.conveyor_triple);

        // --- War system ---
        hide(bl, ModBlocks.war_controller);
        hide(bl, ModItems.sat_war);

        // --- Vanilla furnace replacements (HBM overrides the vanilla furnace block) ---
        hide(bl, ModBlocks.furnace);
        hide(bl, ModBlocks.lit_furnace);

        // --- Celestial bedrock ore grades (non-BASE grades hidden) ---
        // NEI: for(BedrockOreGrade grade : BedrockOreGrade.values()) { if(grade == BASE) continue; ... }
        // Deferred: ItemBedrockOreNew not ported yet.
    }
}
