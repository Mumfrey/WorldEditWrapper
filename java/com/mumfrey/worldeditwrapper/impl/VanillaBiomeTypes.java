package com.mumfrey.worldeditwrapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.biome.BiomeGenBase;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.UnknownBiomeTypeException;

/**
 * Implementation of BiomeTypes
 * 
 * @author Adam Mummery-Smith
 */
public class VanillaBiomeTypes implements BiomeTypes
{
	/**
	 * Singleton
	 */
	private static VanillaBiomeTypes instance;
	
	/**
	 * All biome types
	 */
	private final List<VanillaBiomeType> biomes = new ArrayList<VanillaBiomeType>();
	
	/**
	 * Mapping of Biome gens to biome types
	 */
	private final Map<BiomeGenBase, VanillaBiomeType> biomeToTypeMap = new HashMap<BiomeGenBase, VanillaBiomeType>();
	
	/**
	 * Get the singleton instance
	 */
	public static VanillaBiomeTypes getInstance()
	{
		if (VanillaBiomeTypes.instance == null)
			VanillaBiomeTypes.instance = new VanillaBiomeTypes();
		
		return VanillaBiomeTypes.instance;
	}
	
	/**
	 * Private ctor as per singleton pattern
	 */
	private VanillaBiomeTypes()
	{
		for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
		{
			if (biome != null)
			{
				VanillaBiomeType biomeType = new VanillaBiomeType(biome);
				this.biomes.add(biomeType);
				this.biomeToTypeMap.put(biome, biomeType);
			}
		}
	}
	
	/**
	 * @param biome
	 * @return
	 */
	public BiomeType getFromBaseBiome(BiomeGenBase biome)
	{
		VanillaBiomeType biomeType = this.biomeToTypeMap.get(biome);
		return biomeType != null ? biomeType : BiomeType.UNKNOWN;
	}
	
	/**
	 * @param biomeType
	 * @return
	 */
	public BiomeGenBase getFromBiomeType(VanillaBiomeType biomeType)
	{
		return biomeType != null ? biomeType.getBiome() : null;
	}
	
	/* (non-Javadoc)
	 * @see BiomeTypes#has(java.lang.String)
	 */
	@Override
	public boolean has(String name)
	{
		if (name != null)
		{
			for (VanillaBiomeType biomeType : this.biomes)
			{
				if (biomeType.nameMatches(name))
					return true;
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see BiomeTypes#get(java.lang.String)
	 */
	@Override
	public BiomeType get(String name) throws UnknownBiomeTypeException
	{
		if (name != null)
		{
			for (VanillaBiomeType biomeType : this.biomes)
			{
				if (biomeType.nameMatches(name))
					return biomeType;
			}
		}

		throw new UnknownBiomeTypeException(name);
	}
	
	/* (non-Javadoc)
	 * @see BiomeTypes#all()
	 */
	@Override
	public List<BiomeType> all()
	{
		List<BiomeType> retBiomes = new ArrayList<BiomeType>();
		retBiomes.addAll(this.biomes);
		return retBiomes;
	}
}