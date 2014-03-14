package com.mumfrey.worldeditwrapper.reflect;

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.EntityList;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeDecorator;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;

/**
 * Wrapper for obf/mcp reflection-accessed private fields
 * 
 * @author Adam Mummery-Smith
 * 
 * @param <P> Parent class type, the type of the class that owns the field
 * @param <T> Field type, the type of the field value
 */
@SuppressWarnings("rawtypes")
public class PrivateFields<P, T>
{
	private boolean errorReported;
	
	/**
	 * Name used to access the field, determined at init
	 */
	private final String fieldName;
	
	/**
	 * Class to which this field belongs
	 */
	public final Class<P> parentClass;
	
	/**
	 * Creates a new private field entry
	 * 
	 * @param owner
	 * @param mcpName
	 * @param name
	 */
	private PrivateFields(Class<P> owner, ObfuscationMapping mapping)
	{
		this.parentClass = owner;
		this.fieldName = mapping.getName();
	}
	
	/**
	 * Get the current value of this field on the instance class supplied
	 * 
	 * @param instance Class to get the value of
	 * @return field value or null if errors occur
	 */
	@SuppressWarnings("unchecked")
	public T get(P instance)
	{
		try
		{
			return (T)Reflection.getPrivateValue(this.parentClass, instance, this.fieldName);
		}
		catch (final Exception ex)
		{
			if (!this.errorReported)
			{
				this.errorReported = true;
				ex.printStackTrace();
			}
			return null;
		}
	}
	
	/**
	 * Set the value of this field on the instance class supplied
	 * 
	 * @param instance  Object to set the value of the field on
	 * @param value value to set
	 * @return value
	 */
	public T set(P instance, T value)
	{
		try
		{
			Reflection.setPrivateValue(this.parentClass, instance, this.fieldName, value);
		}
		catch (final Exception ex)
		{
			if (!this.errorReported)
			{
				this.errorReported = true;
				ex.printStackTrace();
			}
		}
		
		return value;
	}
	
	/**
	 * Static private fields
	 * 
	 * @param <P> Parent class type, the type of the class that owns the field
	 * @param <T> Field type, the type of the field value
	 */
	public static final class StaticFields<P, T> extends PrivateFields<P, T>
	{
		public static final StaticFields<EntityList, Map> stringToClassMapping = new StaticFields<EntityList, Map>(EntityList.class, ObfuscationMapping.stringToClassMapping);
		
		@SuppressWarnings("synthetic-access")
		public StaticFields(Class<P> owner, ObfuscationMapping mapping)
		{
			super(owner, mapping);
		}
		
		public T get()
		{
			return this.get(null);
		}
		
		public void set(T value)
		{
			this.set(null, value);
		}
	}

	public static final PrivateFields<ChunkProviderServer, Set>                  chunksToUnload = new PrivateFields<ChunkProviderServer, Set>(ChunkProviderServer.class, ObfuscationMapping.chunksToUnload);
	public static final PrivateFields<ChunkProviderServer, LongHashMap>      loadedChunkHashMap = new PrivateFields<ChunkProviderServer, LongHashMap>(ChunkProviderServer.class, ObfuscationMapping.loadedChunkHashMap);
	public static final PrivateFields<ChunkProviderServer, List<Chunk>>            loadedChunks = new PrivateFields<ChunkProviderServer, List<Chunk>>(ChunkProviderServer.class, ObfuscationMapping.loadedChunks);
	public static final PrivateFields<ChunkProviderServer, IChunkProvider> currentChunkProvider = new PrivateFields<ChunkProviderServer, IChunkProvider>(ChunkProviderServer.class, ObfuscationMapping.currentChunkProvider);
	public static final PrivateFields<BiomeDecorator, World>                       currentWorld = new PrivateFields<BiomeDecorator, World>(BiomeDecorator.class, ObfuscationMapping.currentWorld);
}
