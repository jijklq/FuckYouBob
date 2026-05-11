package com.hbm.blocks;

public interface IBlockMultiPass {

	int getPasses();

	default boolean shouldRenderItemMulti() { return false; }

	// renderID/getRenderType() from 1.7.10 removed — 1.12.2 uses JSON rendering, not int IDs
}
