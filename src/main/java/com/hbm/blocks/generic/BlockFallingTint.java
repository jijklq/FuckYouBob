// LEGACY? Сейчас минимальный stub: random-texture variants и meta→HSB-color
// tint отложены до этапа рендеринга (см. CLAUDE.md «Статус порта»).
package com.hbm.blocks.generic;

import com.hbm.blocks.BlockFallingNT;
import net.minecraft.block.material.Material;

public class BlockFallingTint extends BlockFallingNT {

    public BlockFallingTint(Material mat) {
        super(mat);
    }

    public BlockFallingTint(Material mat, String... extraTextures) {
        super(mat);
    }
}
