package com.hbm.blocks.bomb;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.ITooltipProvider;
import com.hbm.potion.HbmPotion;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTaint extends Block implements ITooltipProvider {

    public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 15);

    public BlockTaint(Material mat) {
        super(mat);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LEVEL);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LEVEL, MathHelper.clamp(meta, 0, 15));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LEVEL);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        return MapColor.GRAY;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        int meta = state.getValue(LEVEL);
        if(meta >= 15) return;
        for(int i = -3; i <= 3; i++) for(int j = -3; j <= 3; j++) for(int k = -3; k <= 3; k++) {
            if(Math.abs(i) + Math.abs(j) + Math.abs(k) > 4) continue;
            if(rand.nextFloat() > 0.25F) continue;
            BlockPos target = pos.add(i, j, k);
            Block b = world.getBlockState(target).getBlock();
            if(world.isAirBlock(target) || b == Blocks.BEDROCK) continue;
            int targetMeta = meta + 1;
            boolean hasAir = false;
            for(EnumFacing dir : EnumFacing.VALUES) {
                if(world.isAirBlock(target.offset(dir))) {
                    hasAir = true;
                    break;
                }
            }
            if(!hasAir) targetMeta = meta + 3;
            if(targetMeta > 15) continue;
            if(b == this && world.getBlockState(target).getValue(LEVEL) >= targetMeta) continue;
            world.setBlockState(target, this.getStateFromMeta(targetMeta), 3);
            if(rand.nextFloat() < 0.25F && BlockFalling.canFallThrough(world.getBlockState(pos.add(i, j - 1, k)))) {
                EntityFallingBlock falling = new EntityFallingBlock(world, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, this.getStateFromMeta(targetMeta));
                world.spawnEntity(falling);
            }
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity) {
        int meta = state.getValue(LEVEL);
        int level = 15 - meta;
        entity.motionX *= 0.6;
        entity.motionZ *= 0.6;
        if(entity instanceof EntityLivingBase) {
            if(world.rand.nextInt(50) == 0 && HbmPotion.taint != null) {
                List<ItemStack> list = new ArrayList<ItemStack>();
                PotionEffect effect = new PotionEffect(HbmPotion.taint, 15 * 20, level);
                effect.setCurativeItems(list);
                ((EntityLivingBase) entity).addPotionEffect(effect);
            }
        }
        // TODO 4.3e mob-transform: requires Entity registration infrastructure + EntityCreeperTainted class.
        // if(entity != null && entity.getClass().equals(EntityCreeper.class)) {
        //     EntityCreeperTainted creep = new EntityCreeperTainted(world);
        //     creep.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        //     if(!world.isRemote) { entity.setDead(); world.spawnEntity(creep); }
        // }

        // TODO 4.3e mob-transform: requires EntityCyberCrab/EntityTeslaCrab port.
        // if(entity instanceof EntityTeslaCrab) {
        //     EntityTaintCrab crab = new EntityTaintCrab(world);
        //     crab.setLocationAndAngles(entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
        //     if(!world.isRemote) { entity.setDead(); world.spawnEntity(crab); }
        // }
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag flag) {
        tooltip.add("DO NOT TOUCH, BREATHE OR STARE AT.");
    }
}
