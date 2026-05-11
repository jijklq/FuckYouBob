package com.hbm.itempool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemPool {

	public static void initialize() {
		// Default pools (ItemPoolsLegacy, ItemPoolsComponent, ...) are populated when their classes are ported.
	}

	public static HashMap<String, ItemPool> pools = new HashMap();

	public String name;
	public WeightedEntry[] pool = new WeightedEntry[0];

	private List<WeightedEntry> buildingList = new ArrayList();

	public ItemPool() { }

	public ItemPool(String name) {
		this.name = name;
		pools.put(name, this);
	}

	public ItemPool add(Item item, int meta, int min, int max, int weight) {	buildingList.add(new WeightedEntry(new ItemStack(item, 1, meta), min, max, weight));							return this; }
	public ItemPool add(Block block, int meta, int min, int max, int weight) {	buildingList.add(new WeightedEntry(new ItemStack(Item.getItemFromBlock(block), 1, meta), min, max, weight));	return this; }
	public ItemPool add(ItemStack item, int min, int max, int weight) {			buildingList.add(new WeightedEntry(item, min, max, weight));													return this; }

	public ItemPool build() {

		this.pool = new WeightedEntry[buildingList.size()];

		for(int i = 0; i < pool.length; i++) {
			this.pool[i] = this.buildingList.get(i);
		}

		this.buildingList.clear();

		return this;
	}

	/** Grabs the specified item pool out of the pool map, will return the backup pool if the given pool is not present */
	public static WeightedEntry[] getPool(String name) {
		ItemPool pool = pools.get(name);
		if(pool == null) return backupPool;
		return pool.pool;
	}

	public static ItemStack getStack(String pool, Random rand) {
		return getStack(ItemPool.getPool(pool), rand);
	}

	public static ItemStack getStack(WeightedEntry[] pool, Random rand) {
		int totalWeight = 0;
		for(WeightedEntry entry : pool) totalWeight += entry.itemWeight;
		int r = totalWeight > 0 ? rand.nextInt(totalWeight) : 0;
		WeightedEntry weighted = pool[0];
		for(WeightedEntry entry : pool) {
			r -= entry.itemWeight;
			if(r < 0) { weighted = entry; break; }
		}
		ItemStack stack = weighted.theItemId.copy();
		stack.setCount(weighted.theMinimumChanceToGenerateItem + rand.nextInt(weighted.theMaximumChanceToGenerateItem - weighted.theMinimumChanceToGenerateItem + 1));
		return stack;
	}

	/** Should a pool be lost due to misconfiguration or otherwise, this pool will be returned in its place */
	private static WeightedEntry[] backupPool = new WeightedEntry[0];

	/** Mirror of the removed vanilla {@code WeightedRandomChestContent}: holds a stack with a min/max count and a weight. */
	public static class WeightedEntry {

		public ItemStack theItemId;
		public int theMinimumChanceToGenerateItem;
		public int theMaximumChanceToGenerateItem;
		public int itemWeight;

		public WeightedEntry(ItemStack stack, int min, int max, int weight) {
			this.theItemId = stack;
			this.theMinimumChanceToGenerateItem = min;
			this.theMaximumChanceToGenerateItem = max;
			this.itemWeight = weight;
		}

		public WeightedEntry(Item item, int meta, int min, int max, int weight) {
			this(new ItemStack(item, 1, meta), min, max, weight);
		}
	}
}
