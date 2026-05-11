package com.hbm.inventory;

import net.minecraft.block.Block;

public class RecipesCommon {

	public static class MetaBlock {

		public Block block;
		public int meta;

		public MetaBlock(Block block, int meta) {
			this.block = block;
			this.meta = meta;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + String.valueOf(block.getRegistryName()).hashCode();
			result = prime * result + meta;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj)
				return true;
			if(obj == null)
				return false;
			if(getClass() != obj.getClass())
				return false;
			MetaBlock other = (MetaBlock) obj;
			if(block == null) {
				if(other.block != null)
					return false;
			} else if(!block.equals(other.block))
				return false;
			if(meta != other.meta)
				return false;
			return true;
		}

		public MetaBlock(Block block) {
			this(block, 0);
		}

		@Deprecated public int getID() {
			return hashCode();
		}
	}
}
