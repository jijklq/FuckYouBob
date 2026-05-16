package com.hbm.blocks.generic;

import java.util.Random;
import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class WasteLeaves extends Block {

    public WasteLeaves(Material mat) {
        super(mat);
        this.setTickRandomly(true);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (rand.nextInt(30) == 0) {
            world.setBlockToAir(pos);
            if (world.getBlockState(pos.down()).getMaterial() == Material.AIR) {
                EntityFallingBlock leaves = new EntityFallingBlock(world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        ModBlocks.leaves_layer.getDefaultState());
                leaves.fallTime = 2;
                leaves.shouldDropItem = false;
                world.spawnEntity(leaves);
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(state, world, pos, rand);
        if (rand.nextInt(7) == 0 && world.getBlockState(pos.down()).getMaterial() == Material.AIR) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "deadleaf");
            data.setDouble("posX", pos.getX() + rand.nextDouble());
            data.setDouble("posY", pos.getY() - 0.05);
            data.setDouble("posZ", pos.getZ() + rand.nextDouble());
            MainRegistry.proxy.effectNT(data);
        }
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    protected boolean canSilkHarvest() {
        return false;
    }
}
