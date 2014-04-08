package com.mumfrey.worldeditwrapper.impl;

import java.util.ArrayList;
import java.util.List;

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
	 * Valid names for this biome
	 */
	private final List<String> biomeNames = new ArrayList<String>();
	
	/**
	 * @param biome
	 */
	public VanillaBiomeType(BiomeGenBase biome)
	{
		this.biome = biome;
		
		String biomeName = biome.biomeName.toLowerCase();
		this.biomeNames.add(biomeName);
		this.biomeNames.add(biomeName.replace(' ', '_'));
		this.biomeNames.add(biomeName.replace(" ", ""));
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

	public boolean nameMatches(String name)
	{
		return this.biomeNames.contains(name.toLowerCase());
	}
}