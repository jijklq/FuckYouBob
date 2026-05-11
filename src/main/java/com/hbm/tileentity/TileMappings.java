package com.hbm.tileentity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.tileentity.TileEntity;

public class TileMappings {

	public static HashMap<Class<? extends TileEntity>, String[]> map = new HashMap<Class<? extends TileEntity>, String[]>();
	public static List<Class<? extends IConfigurableMachine>> configurables = new ArrayList<Class<? extends IConfigurableMachine>>();
}
