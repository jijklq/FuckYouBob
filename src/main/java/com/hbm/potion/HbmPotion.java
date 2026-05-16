package com.hbm.potion;

import net.minecraft.potion.Potion;

public class HbmPotion {
    // null до radiation-этапа; коллеры обязаны null-check'ать
    // перед new PotionEffect(HbmPotion.radiation, ...).
    public static Potion radiation = null;
    public static Potion taint = null;
}
