package com.hbm.main;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = MainRegistry.MOD_ID, value = Side.CLIENT)
public class ClientRegistry {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (ResourceLocation key : Item.REGISTRY.getKeys()) {
            if (MainRegistry.MOD_ID.equals(key.getResourceDomain())) {
                Item item = Item.REGISTRY.getObject(key);
                if (item != null) {
                    ModelLoader.setCustomModelResourceLocation(item, 0,
                            new ModelResourceLocation(key, "inventory"));
                }
            }
        }
    }
}
