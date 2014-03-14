package com.mumfrey.worldeditwrapper.reflect;

import com.mumfrey.liteloader.util.ModUtilities;

/**
 * Central list of obfuscation mappings used throughout macros, coalesced here
 * instead of being spread throughout the different reflection mechanisms
 * 
 * @author Adam Mummery-Smith
 * 
 * TODO Obfuscation - updated 1.7.2
 */
public enum ObfuscationMapping
{
	stringToClassMapping("stringToClassMapping", "c", "field_75625_b"),
	      chunksToUnload("chunksToUnload",       "c", "field_73248_b"),
	  loadedChunkHashMap("loadedChunkHashMap",   "g", "field_73244_f"),
	        loadedChunks("loadedChunks",         "h", "field_73245_g"),
	currentChunkProvider("currentChunkProvider", "e", "field_73246_d"),
	        currentWorld("currentWorld",         "a", "field_76815_a");
	
	
	public final String mcpName;
	
	public final String obfuscatedName;
	
	public final String seargeName;
	
	private ObfuscationMapping(String mcpName, String obfuscatedName, String seargeName)
	{
		this.mcpName = mcpName != null ? mcpName : seargeName;
		this.obfuscatedName = obfuscatedName;
		this.seargeName = seargeName;
	}
	
	public String getName()
	{
		return ModUtilities.getObfuscatedFieldName(this.mcpName, this.obfuscatedName, this.seargeName);
	}
}
