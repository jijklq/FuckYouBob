package com.hbm.blocks.generic;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.hbm.blocks.BlockEnumMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockOreBasalt extends BlockEnumMulti {

    public static final PropertyInteger VARIANT = PropertyInteger.create("variant", 0, 4);

    public BlockOreBasalt() {
        super(Material.ROCK, EnumBasaltOreType.class, true);
    }

    @Override
    protected PropertyInteger getVariantProperty() { return VARIANT; }

    public static enum EnumBasaltOreType {
        SULFUR,
        FLUORITE,
        ASBESTOS,
        GEM,
        MOLYSITE;

        public Item drop() {
            switch (this) {
                case SULFUR:   return ModItems.sulfur;
                case FLUORITE: return ModItems.fluorite;
                case ASBESTOS: return ModItems.ingot_asbestos;
                case GEM:      return ModItems.gem_volcanic;
                case MOLYSITE: return ModItems.powder_molysite;
                default:       return null;
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        EnumBasaltOreType type = EnumUtil.grabEnumSafely(EnumBasaltOreType.class, getMetaFromState(state));
        return type.drop();
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        int meta = getMetaFromState(world.getBlockState(pos));
        if (meta == EnumBasaltOreType.ASBESTOS.ordinal() && world.getBlockState(pos.up()).getBlock() == Blocks.AIR) {
            if (world.rand.nextInt(10) == 0) world.setBlockState(pos.up(), ModBlocks.gas_asbestos.getDefaultState());
            for (int i = 0; i < 5; i++)
                world.spawnParticle(EnumParticleTypes.TOWN_AURA,
                        pos.getX() + world.rand.nextFloat(), pos.getY() + 1.1, pos.getZ() + world.rand.nextFloat(),
                        0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) { } //no more BUD outgassing for you, mister

    @Override
    public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
        if (getMetaFromState(state) == EnumBasaltOreType.ASBESTOS.ordinal())
            worldIn.setBlockState(pos, ModBlocks.gas_asbestos.getDefaultState());
        super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess worldIn, BlockPos pos, IBlockState state, int fortune) {
        return ModBlocks.getDropsWithoutDamage(this, state, fortune, new Random());
    }
}
