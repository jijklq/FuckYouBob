package com.hbm.blocks.generic;

import java.util.Random;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.potion.HbmPotion;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockStepEffect extends Block {

    public BlockStepEffect(Material mat) {
        super(mat);
    }

    public BlockStepEffect(Material mat, boolean tick) {
        super(mat);
        this.setTickRandomly(tick);
    }

    public boolean allowFortune = true;

    public BlockStepEffect noFortune() {
        this.allowFortune = false;
        return this;
    }

    @Override
    public int quantityDroppedWithBonus(int fortune, Random rand) {
        if (fortune > 0 && Item.getItemFromBlock(this) != this.getItemDropped(this.getDefaultState(), rand, fortune) && allowFortune) {
            int mult = rand.nextInt(fortune + 2) - 1;
            if (mult < 0) mult = 0;
            return this.quantityDropped(rand) * (mult + 1);
        } else {
            return this.quantityDropped(rand);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (this == ModBlocks.waste_trinitite || this == ModBlocks.waste_trinitite_red) return ModItems.trinitite;
        if (this == ModBlocks.waste_planks) return Items.COAL;
        if (this == ModBlocks.frozen_dirt) return Items.SNOWBALL;
        if (this == ModBlocks.frozen_planks) return Items.SNOWBALL;
        if (this == ModBlocks.ore_nether_fire) return rand.nextInt(10) == 0 ? ModItems.ingot_phosphorus : ModItems.powder_fire;
        if (this == ModBlocks.block_meteor_cobble) return ModItems.fragment_meteorite;
        if (this == ModBlocks.block_meteor_broken) return ModItems.fragment_meteorite;
        if (this == ModBlocks.block_meteor_molten) return Items.AIR;
        if (this == ModBlocks.ore_nether_cobalt) return ModItems.fragment_cobalt;
        if (this == ModBlocks.ore_nether_sulfur) return ModItems.sulfur;
        if (this == ModBlocks.ore_gneiss_rare) return ModItems.chunk_ore;
        if (this == ModBlocks.ore_gneiss_asbestos) return ModItems.ingot_asbestos;
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random rand) {
        if (this == ModBlocks.ore_nether_sulfur) return 2 + rand.nextInt(3);
        if (this == ModBlocks.block_meteor_broken) return 1 + rand.nextInt(3);
        if (this == ModBlocks.block_meteor_treasure) return 1 + rand.nextInt(3);
        if (this == ModBlocks.ore_nether_cobalt) return 5 + rand.nextInt(8);
        return 1;
    }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            if (this == ModBlocks.frozen_dirt)
                living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2 * 60 * 20, 2));
            if (this == ModBlocks.block_trinitite && HbmPotion.radiation != null)
                living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, 2));
            if (this == ModBlocks.block_waste && HbmPotion.radiation != null)
                living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, 2));
            if ((this == ModBlocks.waste_trinitite || this == ModBlocks.waste_trinitite_red) && HbmPotion.radiation != null)
                living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, 0));
            if (this == ModBlocks.brick_jungle_ooze && HbmPotion.radiation != null)
                living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 15 * 20, 9));
            if (this == ModBlocks.brick_jungle_mystic && HbmPotion.taint != null)
                living.addPotionEffect(new PotionEffect(HbmPotion.taint, 15 * 20, 2));
        }
        if (this == ModBlocks.block_meteor_molten) entity.setFire(5);
    }

    @Override
    public int damageDropped(IBlockState state) {
        // TODO restore when EnumChunkType is ported: if(this == ModBlocks.ore_gneiss_rare) return EnumChunkType.RARE.ordinal();
        return this == ModBlocks.waste_planks ? 1 : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
        if (this == ModBlocks.waste_trinitite || this == ModBlocks.waste_trinitite_red
                || this == ModBlocks.block_trinitite || this == ModBlocks.block_waste) {
            world.spawnParticle(EnumParticleTypes.TOWN_AURA,
                    pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(),
                    0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this == ModBlocks.block_meteor_molten) {
            if (!world.isRemote) world.setBlockState(pos, ModBlocks.block_meteor_cobble.getDefaultState());
            world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS,
                    0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
        }
    }

    @Override
    public void onBlockDestroyedByPlayer(World world, BlockPos pos, IBlockState state) {
        if (this == ModBlocks.block_meteor_molten) {
            if (!world.isRemote) world.setBlockState(pos, Blocks.LAVA.getDefaultState());
        }
    }
}
