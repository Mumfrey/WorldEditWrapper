package com.mumfrey.worldeditwrapper.impl.undo;

import net.minecraft.world.WorldProvider;

public class UndoWorldProxyProvider extends WorldProvider
{
	@Override
	public String getDimensionName()
	{
		return "undo";
	}

    @Override
    protected void registerWorldChunkManager()
    {
    }
    
    @Override
    protected void generateLightBrightnessTable()
    {
    }
}
