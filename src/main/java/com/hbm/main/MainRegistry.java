package com.hbm.main;

import com.hbm.blocks.ModBlocks;
import com.hbm.creativetabs.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MainRegistry.MOD_ID, name = MainRegistry.MOD_NAME, version = MainRegistry.MOD_VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class MainRegistry {

    public static final String MOD_ID = "hbm";
    public static final String MOD_NAME = "HBM's Nuclear Tech Mod";
    public static final String MOD_VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static MainRegistry instance;

    @SidedProxy(
        clientSide = "com.hbm.main.ClientProxy",
        serverSide = "com.hbm.main.CommonProxy"
    )
    public static CommonProxy proxy;

    public static CreativeTabs partsTab      = new PartsTab("tabParts");
    public static CreativeTabs controlTab    = new ControlTab("tabControl");
    public static CreativeTabs templateTab   = new TemplateTab("tabTemplate");
    public static CreativeTabs blockTab      = new BlockTab("tabBlocks");
    public static CreativeTabs machineTab    = new MachineTab("tabMachine");
    public static CreativeTabs nukeTab       = new NukeTab("tabNuke");
    public static CreativeTabs missileTab    = new MissileTab("tabMissile");
    public static CreativeTabs weaponTab     = new WeaponTab("tabWeapon");
    public static CreativeTabs consumableTab = new ConsumableTab("tabConsumable");

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
