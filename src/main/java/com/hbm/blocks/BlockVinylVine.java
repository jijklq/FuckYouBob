package com.hbm.blocks;

import net.minecraft.block.BlockVine;

// Attachment to BlockRubberLeaves (non-opaque leaves) is a worldgen concern — handled when TTree is ported.
// In 1.12.2, BlockVine's stay logic is internal (neighborChanged → canAttachTo, which is private),
// so we cannot replicate the 1.7.10 canBlockStay/canPlaceBlockOn overrides directly.
public class BlockVinylVine extends BlockVine {

    public BlockVinylVine() {
        super();
    }
}
