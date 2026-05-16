package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WasteEarth extends Block {

    public WasteEarth(Material mat, boolean tick, SoundType sound) {
        super(mat);
        this.setTickRandomly(tick);
        this.setSoundType(sound);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if(this == ModBlocks.waste_earth || this == ModBlocks.waste_mycelium || this == ModBlocks.burning_earth) {
            return Item.getItemFromBlock(Blocks.DIRT);
        }
        if(this == ModBlocks.frozen_grass) {
            return Items.SNOWBALL;
        }
        return Item.getItemFromBlock(this);
    }

    @Override
    public int quantityDropped(Random rand) { return 1; }

    @Override
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        if(world.isRemote) return;
        if(entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            if(this == ModBlocks.frozen_grass) {
                living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 2 * 60 * 20, 2));
            }
            if(this == ModBlocks.waste_mycelium && HbmPotion.radiation != null) {
                living.addPotionEffect(new PotionEffect(HbmPotion.radiation, 30 * 20, 3));
            }
            if(this == ModBlocks.burning_earth) {
                living.setFire(5);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if(this == ModBlocks.waste_mycelium) {
            world.spawnParticle(EnumParticleTypes.TOWN_AURA, x + rand.nextFloat(), y + 1.1D, z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
        if(this == ModBlocks.burning_earth) {
            world.spawnParticle(EnumParticleTypes.FLAME, x + rand.nextFloat(), y + 1.1D, z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + rand.nextFloat(), y + 1.1D, z + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if(this == ModBlocks.waste_mycelium && GeneralConfig.enableMycelium) {
            for(int i = -1; i < 2; i++) {
                for(int j = -1; j < 2; j++) {
                    for(int k = -1; k < 2; k++) {
                        BlockPos neighbor = pos.add(i, j, k);
                        Block b0 = world.getBlockState(neighbor).getBlock();
                        IBlockState b1State = world.getBlockState(neighbor.up());
                        if(!b1State.isOpaqueCube() && (b0 == Blocks.DIRT || b0 == Blocks.GRASS || b0 == Blocks.MYCELIUM || b0 == ModBlocks.waste_earth)) {
                            world.setBlockState(neighbor, ModBlocks.waste_mycelium.getDefaultState());
                        }
                    }
                }
            }
        }
        if(this == ModBlocks.burning_earth) {
            if(rand.nextInt(5) == 0) {
                for(int i = -1; i < 2; i++) {
                    for(int j = -1; j < 2; j++) {
                        for(int k = -1; k < 2; k++) {
                            BlockPos neighbor = pos.add(i, j, k);
                            if(!world.isBlockLoaded(neighbor)) continue;
                            IBlockState b0State = world.getBlockState(neighbor);
                            Block b0 = b0State.getBlock();
                            BlockPos neighborUp = neighbor.up();
                            IBlockState b1State = world.getBlockState(neighborUp);
                            Block b1 = b1State.getBlock();
                            if(!b1State.isOpaqueCube() &&
                                    ((b0 == Blocks.GRASS || b0 == Blocks.MYCELIUM || b0 == ModBlocks.waste_earth ||
                                    b0 == ModBlocks.frozen_grass || b0 == ModBlocks.waste_mycelium)
                                    && !world.isRainingAt(pos))) {
                                world.setBlockState(neighbor, ModBlocks.burning_earth.getDefaultState());
                            }
                            if(b0 instanceof BlockLeaves || b0 instanceof BlockBush) {
                                world.setBlockToAir(neighbor);
                            }
                            if(b0 == ModBlocks.frozen_dirt) {
                                world.setBlockState(neighbor, Blocks.DIRT.getDefaultState());
                            }
                            if(b1State.getBlock().isFlammable(world, neighborUp, EnumFacing.UP) && !(b1 instanceof BlockLeaves || b1 instanceof BlockBush) && world.isAirBlock(pos.up())) {
                                world.setBlockState(pos.up(), Blocks.FIRE.getDefaultState());
                            }
                        }
                    }
                }
            }
            world.setBlockState(pos, ModBlocks.impact_dirt.getDefaultState());
        }
        if(this == ModBlocks.waste_earth || this == ModBlocks.waste_mycelium) {
            BlockPos up = pos.up();
            if(RadiationConfig.cleanupDeadDirt || (world.getLightFromNeighbors(up) < 4 && world.getBlockState(up).getLightOpacity(world, up) > 2)) {
                world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            }
            if(world.getBlockState(up).getBlock() instanceof BlockMushroom) {
                world.setBlockState(up, ModBlocks.mush.getDefaultState());
            }
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block fromBlock, BlockPos fromPos) {
        if(this == ModBlocks.burning_earth) {
            IBlockState upState = world.getBlockState(pos.up());
            Block b = upState.getBlock();
            if(b instanceof BlockLiquid || b instanceof BlockFluidBase || upState.isNormalCube()) {
                world.setBlockState(pos, Blocks.DIRT.getDefaultState());
            }
        }
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing direction, IPlantable plantable) {
        if(this == ModBlocks.waste_earth || this == ModBlocks.waste_mycelium) {
            return plantable.getPlantType(world, pos.up()) == EnumPlantType.Cave;
        }
        return false;
    }
}
