package com.hbm.main;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.*;
import com.hbm.creativetabs.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

@Mod(modid = MainRegistry.MOD_ID, name = MainRegistry.MOD_NAME, version = MainRegistry.MOD_VERSION, acceptedMinecraftVersions = "[1.12.2]")
public class MainRegistry {

    public static final String MOD_ID = "hbm";
    public static final String MOD_NAME = "HBM's Nuclear Tech Mod";
    public static final String MOD_VERSION = "1.0.0";

    @Mod.Instance(MOD_ID)
    public static MainRegistry instance;

    public static final Logger logger = LogManager.getLogger(MOD_ID);

    public static File configHbmDir;

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
        configHbmDir = new File(event.getModConfigurationDirectory(), "hbmConfig");
        configHbmDir.mkdirs();
        loadConfig(event);
        proxy.preInit(event);
    }

    private void loadConfig(FMLPreInitializationEvent event) {
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();

        GeneralConfig.loadFromConfig(config);
        WorldConfig.loadFromConfig(config);
        MachineConfig.loadFromConfig(config);
        BombConfig.loadFromConfig(config);
        RadiationConfig.loadFromConfig(config);
        PotionConfig.loadFromConfig(config);
        ToolConfig.loadFromConfig(config);
        WeaponConfig.loadFromConfig(config);
        MobConfig.loadFromConfig(config);
        StructureConfig.loadFromConfig(config);
        SpaceConfig.loadFromConfig(config);

        config.save();

        try {
            if(GeneralConfig.enableThermosPreventer && Class.forName("thermos.ThermosClassTransformer") != null) {
                throw new IllegalStateException("The mod tried to start on a Thermos or its fork server and therefore stopped. To allow the server to start on Thermos, change the appropriate "
                    + "config entry (0.00 in hbm.cfg). This was done because, by default, Thermos "
                    + "uses a so-called \"optimization\" feature that reduces tile ticking a lot, which will inevitably break a lot of machines. Most people aren't even aware "
                    + "of this, and start blaming random mods for all their stuff breaking. In order to adjust or even disable this feature, edit \"tileentities.yml\" in your "
                    + "Thermos install folder. If you believe that crashing the server until a config option is changed is annoying, then I would agree, but it's still preferable "
                    + "over wasting hours trying to fix an issue that is really just an \"intended feature\" added by Thermos itself, and not a bug in the mod. You'll have to "
                    + "change Thermos' config anyway so that extra change in NTM's config can't be that big of a burden.");
            }
        } catch(ClassNotFoundException e) { }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ServerConfig.initConfig();
        MachineDynConfig.initialize();
        FalloutConfigJSON.initialize();
        ItemPoolConfigJSON.initialize();
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
