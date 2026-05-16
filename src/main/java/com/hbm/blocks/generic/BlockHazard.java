package com.hbm.blocks.generic;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.hazard.HazardRegistry;
import com.hbm.hazard.HazardSystem;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class BlockHazard extends Block implements ITooltipProvider {

    protected float rad = 0.0F;
    private ExtDisplayEffect extEffect = null;
    private boolean beaconable = false;

    public BlockHazard() { this(Material.IRON); }
    public BlockHazard(Material mat) { super(mat); }

    public BlockHazard setDisplayEffect(ExtDisplayEffect extEffect) {
        this.extEffect = extEffect;
        return this;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
        if(extEffect == null) return;
        switch(extEffect) {
            case RADFOG: case SCHRAB: case FLAMES:
                sPart(world, pos, rand);
                break;
            case SPARKS: break;
            case LAVAPOP:
                world.spawnParticle(EnumParticleTypes.LAVA,
                        pos.getX() + rand.nextFloat(), pos.getY() + 1.1F, pos.getZ() + rand.nextFloat(),
                        0.0D, 0.0D, 0.0D);
                break;
            default: break;
        }
    }

    private void sPart(World world, BlockPos pos, Random rand) {
        for(EnumFacing dir : EnumFacing.VALUES) {
            if(dir == EnumFacing.DOWN && this.extEffect == ExtDisplayEffect.FLAMES) continue;
            BlockPos neighbor = pos.offset(dir);
            if(world.getBlockState(neighbor).getMaterial() == Material.AIR) {
                double ix = pos.getX() + 0.5 + dir.getFrontOffsetX() + rand.nextDouble() * 3 - 1.5;
                double iy = pos.getY() + 0.5 + dir.getFrontOffsetY() + rand.nextDouble() * 3 - 1.5;
                double iz = pos.getZ() + 0.5 + dir.getFrontOffsetZ() + rand.nextDouble() * 3 - 1.5;
                if(dir.getFrontOffsetX() != 0) ix = pos.getX() + 0.5 + dir.getFrontOffsetX() * 0.5 + rand.nextDouble() * dir.getFrontOffsetX();
                if(dir.getFrontOffsetY() != 0) iy = pos.getY() + 0.5 + dir.getFrontOffsetY() * 0.5 + rand.nextDouble() * dir.getFrontOffsetY();
                if(dir.getFrontOffsetZ() != 0) iz = pos.getZ() + 0.5 + dir.getFrontOffsetZ() * 0.5 + rand.nextDouble() * dir.getFrontOffsetZ();
                if(this.extEffect == ExtDisplayEffect.RADFOG) {
                    world.spawnParticle(EnumParticleTypes.TOWN_AURA, ix, iy, iz, 0.0, 0.0, 0.0);
                }
                if(this.extEffect == ExtDisplayEffect.SCHRAB) {
                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "schrabfog");
                    data.setDouble("posX", ix);
                    data.setDouble("posY", iy);
                    data.setDouble("posZ", iz);
                    MainRegistry.proxy.effectNT(data);
                }
                if(this.extEffect == ExtDisplayEffect.FLAMES) {
                    world.spawnParticle(EnumParticleTypes.FLAME, ix, iy, iz, 0.0, 0.0, 0.0);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.0, 0.0);
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, ix, iy, iz, 0.0, 0.1, 0.0);
                }
            }
        }
    }

    public BlockHazard makeBeaconable() {
        this.beaconable = true;
        return this;
    }

    @Override
    public boolean isBeaconBase(IBlockAccess worldObj, BlockPos pos, BlockPos beacon) {
        return beaconable;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if(this.rad > 0) {
            ChunkRadiationManager.proxy.incrementRad(world, pos, rad);
            world.scheduleUpdate(pos, this, this.tickRate(world));
        }
    }

    @Override
    public int tickRate(World world) {
        if(this.rad > 0) return 20;
        return super.tickRate(world);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        rad = HazardSystem.getHazardLevelFromStack(new ItemStack(this), HazardRegistry.RADIATION) * 0.1F;
        if(this.rad > 0)
            world.scheduleUpdate(pos, this, this.tickRate(world));
    }

    public static enum ExtDisplayEffect {
        RADFOG, SPARKS, SCHRAB, FLAMES, LAVAPOP
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) { }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        if(this == ModBlocks.block_schraranium
                || this == ModBlocks.block_schraranium
                || this == ModBlocks.block_schrabidate
                || this == ModBlocks.block_solinium
                || this == ModBlocks.block_schrabidium_fuel)
            return EnumRarity.RARE;
        return EnumRarity.COMMON;
    }
}
