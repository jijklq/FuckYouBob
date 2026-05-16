package com.hbm.saveddata;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraft.world.storage.MapStorage;

public class TomSaveData extends WorldSavedData {

    public final static String key = "impactData";
    public float dust;
    public float fire;
    public boolean impact;
    public long time;
    public long dtime;
    public int x;
    public int z;

    private static TomSaveData lastCachedUnsafe = null;

    public static TomSaveData forWorld(World world) {
        MapStorage storage = world.getPerWorldStorage();
        TomSaveData result = (TomSaveData) storage.getOrLoadData(TomSaveData.class, key);
        if(result == null) {
            result = new TomSaveData(key);
            storage.setData(key, result);
        }
        lastCachedUnsafe = result;
        return result;
    }

    public static TomSaveData getLastCachedOrNull() { return lastCachedUnsafe; }
    public static void resetLastCached() { lastCachedUnsafe = null; }

    public TomSaveData(String tagName) { super(tagName); }

    @Override
    public void readFromNBT(NBTTagCompound c) {
        this.dust = c.getFloat("dust");
        this.fire = c.getFloat("fire");
        this.impact = c.getBoolean("impact");
        this.time = c.getLong("time");
        this.dtime = c.getLong("dtime");
        this.x = c.getInteger("x");
        this.z = c.getInteger("z");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setFloat("dust", dust);
        nbt.setFloat("fire", fire);
        nbt.setBoolean("impact", impact);
        nbt.setLong("time", time);
        nbt.setLong("dtime", dtime);
        nbt.setInteger("x", x);
        nbt.setInteger("z", z);
        return nbt;
    }
}
