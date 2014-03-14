package com.mumfrey.worldeditwrapper.impl;

import com.sk89q.worldedit.BiomeType;

import net.minecraft.world.biome.BiomeGenBase;

/**
 * Vanilla implementation of BiomeType
 * 
 * @author Adam Mummery-Smith
 */
public class VanillaBiomeType implements BiomeType
{
	/**
	 * 
	 */
	private final BiomeGenBase biome;
	
	/**
	 * @param biome
	 */
	public VanillaBiomeType(BiomeGenBase biome)
	{
		this.biome = biome;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.BiomeType#getName()
	 */
	@Override
	public String getName()
	{
		return this.biome.biomeName;
	}
	
	/**
	 * @return
	 */
	public BiomeGenBase getBiome()
	{
		return this.biome;
	}
}