package com.mumfrey.worldeditwrapper.impl;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.LongHashMap;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.feature.*;

import com.mumfrey.worldeditwrapper.impl.undo.UndoWorldProxy;
import com.mumfrey.worldeditwrapper.reflect.PrivateFields;
import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EntityType;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.foundation.Block;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;

/**
 * Wrapper for vanilla world
 * 
 * @author Adam Mummery-Smith
 */
public class VanillaWorld extends LocalWorld
{
	/**
	 * The world
	 */
	private final WeakReference<World> world;
	
	/**
	 * @param world
	 * @return
	 */
	public static VanillaWorld getLocalWorld(World world)
	{
		return new VanillaWorld(world);
	}
	
	/**
	 * @param world
	 */
	private VanillaWorld(World world)
	{
		this.world = new WeakReference<World>(world);
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#getName()
	 */
	@Override
	public String getName()
	{
		return this.world.get().provider.getDimensionName();
	}
	
	/**
	 * @return
	 */
	public World getWorld()
	{
		return this.world.get();
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#clearContainerBlockContents(com.sk89q.worldedit.Vector)
	 */
	@Override
	public boolean clearContainerBlockContents(Vector pt)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			TileEntity tileEntity = theWorld.getTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
			if ((tileEntity instanceof IInventory))
			{
				IInventory inventory = (IInventory)tileEntity;
				for (int i = 0; i < inventory.getSizeInventory(); i++)
				{
					inventory.setInventorySlotContents(i, null);
				}
				
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#copyFromWorld(com.sk89q.worldedit.Vector, com.sk89q.worldedit.blocks.BaseBlock)
	 */
	@Override
	public boolean copyFromWorld(Vector pt, BaseBlock block)
	{
		World theWorld = this.world.get();
		if (block instanceof VanillaTileEntityBlock && theWorld != null)
		{
			((VanillaTileEntityBlock)block).setTileEntity(theWorld.getTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ()));
			return true;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#copyToWorld(com.sk89q.worldedit.Vector, com.sk89q.worldedit.blocks.BaseBlock)
	 */
	@Override
	public boolean copyToWorld(Vector pt, BaseBlock block)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			TileEntity newTileEntity = this.createCopyAt(pt, block);
			
			if (newTileEntity != null)
			{
				theWorld.removeTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
				theWorld.setTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newTileEntity);
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @param pt
	 * @param block
	 * @return
	 */
	public TileEntity createCopyAt(Vector pt, BaseBlock block)
	{
		if (block instanceof SignBlock)
		{
			TileEntitySign tileEntitySign = new TileEntitySign();
			tileEntitySign.signText = ((SignBlock)block).getText();
			return tileEntitySign;
		}
		else if (block instanceof MobSpawnerBlock)
		{
			TileEntityMobSpawner tileEntityMobSpawner = new TileEntityMobSpawner();
			tileEntityMobSpawner.func_145881_a().setEntityName(((MobSpawnerBlock)block).getMobType());
			return tileEntityMobSpawner;
		}
		else if (block instanceof NoteBlock)
		{
			TileEntityNote tileEntityNote = new TileEntityNote();
			tileEntityNote.note = ((NoteBlock)block).getNote();
			return tileEntityNote;
		}
		else if (block instanceof SkullBlock)
		{
			TileEntitySkull tileEntitySkull = new TileEntitySkull();
			tileEntitySkull.func_145905_a(((SkullBlock)block).getSkullType(), ((SkullBlock)block).getOwner());
			tileEntitySkull.setSkullRotation(((SkullBlock)block).getRot());
			return tileEntitySkull;
		}
		else if (block instanceof VanillaTileEntityBlock)
		{
			return ((VanillaTileEntityBlock)block).createCopyAt(pt);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#dropItem(com.sk89q.worldedit.Vector, com.sk89q.worldedit.blocks.BaseItemStack)
	 */
	@Override
	public void dropItem(Vector pt, BaseItemStack item)
	{
		if ((item == null) || (item.getType() == 0))
			return;
		
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			EntityItem entity = new EntityItem(theWorld, pt.getX(), pt.getY(), pt.getZ(), VanillaWorld.getItemStack(item));
			entity.delayBeforeCanPickup = 10;
			theWorld.spawnEntityInWorld(entity);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#getBiome(com.sk89q.worldedit.Vector2D)
	 */
	@Override
	public BiomeType getBiome(Vector2D pt)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			return VanillaBiomeTypes.getInstance().getFromBaseBiome(theWorld.getBiomeGenForCoords(pt.getBlockX(), pt.getBlockZ()));
		}
		
		return BiomeType.UNKNOWN;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#getBlock(com.sk89q.worldedit.Vector)
	 */
	@Override
	public BaseBlock getBlock(Vector pt)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			int type = this.getBlockType(pt);
			int data = this.getBlockData(pt);
			
			final TileEntity tileEntity = theWorld.getTileEntity(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
			
			if (tileEntity != null)
			{
				final VanillaTileEntityBlock block = new VanillaTileEntityBlock(type, data, tileEntity);
				this.copyFromWorld(pt, block);
				
				return block;
			}
			
			return new BaseBlock(type, data);
		}
		
		return new BaseBlock(0, 0);
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#getBlockData(com.sk89q.worldedit.Vector)
	 */
	@Override
	public int getBlockData(Vector pt)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			return theWorld.getBlockMetadata(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
		}
		
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#getBlockLightLevel(com.sk89q.worldedit.Vector)
	 */
	@Override
	public int getBlockLightLevel(Vector pt)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			return theWorld.getBlockLightValue(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
		}
		
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#getBlockType(com.sk89q.worldedit.Vector)
	 */
	@Override
	public int getBlockType(Vector pt)
	{
		return VanillaWorld.getIdFromBlock(this.getBlockAt(pt));
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#isValidBlockType(int)
	 */
	@Override
	public boolean isValidBlockType(int id)
	{
		return (id == 0) || (VanillaWorld.getBlockById(id) != null);
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#killMobs(com.sk89q.worldedit.Vector, double, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int killMobs(Vector origin, double radius, int flags)
	{
		int deathCount = 0;
		
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			boolean killPets    = (flags & KillFlags.PETS)    == KillFlags.PETS;   
			boolean killNPCs    = (flags & KillFlags.NPCS)    == KillFlags.NPCS;   
			boolean killAnimals = (flags & KillFlags.ANIMALS) == KillFlags.ANIMALS;
			boolean killGolems  = (flags & KillFlags.GOLEMS)  == KillFlags.GOLEMS;
			boolean killAmbient = (flags & KillFlags.AMBIENT) == KillFlags.AMBIENT;
			
			double sqRadius = radius * radius;
			
			for (Entity entity : (List<Entity>)theWorld.loadedEntityList)
			{
				if (entity instanceof EntityLiving)
				{
					double distanceTo = (radius < 0) ? 0 : origin.distanceSq(new Vector(entity.posX, entity.posY, entity.posZ));
					
					if (distanceTo <= sqRadius)
					{
						if ((killAnimals && entity instanceof EntityAnimal)
						||  (killPets    && entity instanceof EntityTameable && ((EntityTameable)entity).isTamed())
						||  (killGolems  && entity instanceof EntityGolem)
						||  (killNPCs    && entity instanceof EntityVillager)
						||  (killAmbient && entity instanceof EntityAmbientCreature))
						{
							entity.setDead();
							deathCount++;
						}
					}
				}
			}
		}
		
		return deathCount;
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#removeEntities(com.sk89q.worldedit.EntityType, com.sk89q.worldedit.Vector, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int removeEntities(EntityType type, Vector origin, int radius)
	{
		int removedCount = 0;

		World theWorld = this.world.get();
		if (theWorld != null)
		{
			double sqRadius = radius * radius;
			
			for (Entity entity : (List<Entity>)theWorld.loadedEntityList)
			{
				if (entity instanceof EntityLiving)
				{
					double distanceTo = (radius < 0) ? 0 : origin.distanceSq(new Vector(entity.posX, entity.posY, entity.posZ));
					
					if (distanceTo <= sqRadius && VanillaWorld.isEntityOfType(entity, type))
					{
						entity.isDead = true;
						removedCount++;
					}
				}
			}
		}
		
		return removedCount;
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#regenerate(com.sk89q.worldedit.regions.Region, com.sk89q.worldedit.EditSession)
	 */
	@Override
	public boolean regenerate(Region region, EditSession editSession)
	{
		boolean result = false;
		
		Set<Vector2D> chunks = region.getChunks();
		for (Vector2D chunk : chunks)
		{
			World theWorld = this.world.get();
			if (theWorld != null)
			{
				Vector chunkCoords = new Vector(chunk.getBlockX() * 16, 0, chunk.getBlockZ() * 16);
				BaseBlock[] chunkData = this.getChunkData(chunkCoords, editSession);
				
				if (this.regenChunk(chunk, theWorld))
				{
					result = true;
					this.applyChanges(chunkCoords, chunkData, editSession, region, theWorld);
				}
				else
				{
					result = false;
				}
			}
		}
		
		return result;
	}

	/**
	 * @param chunkCoords
	 * @param history
	 * @param session
	 * @return 
	 */
	public BaseBlock[] getChunkData(Vector chunkCoords, EditSession session)
	{
		BaseBlock[] chunkData = new BaseBlock[16 * 16 * (this.getMaxY() + 1)];
		
		for (int x = 0; x < 16; x++)
		{
			for (int y = 0; y <= this.getMaxY(); y++)
			{
				for (int z = 0; z < 16; z++)
				{
					Vector pt = chunkCoords.add(x, y, z);
					int index = y * 16 * 16 + z * 16 + x;
					chunkData[index] = session.getBlock(pt);
				}
			}
		}
		
		return chunkData;
	}

	/**
	 * @param region
	 * @param world
	 * @return
	 */
	public boolean regenChunk(Vector2D chunkCoords, World world)
	{
		try
		{
			IChunkProvider provider = world.getChunkProvider();
			
			if (!(provider instanceof ChunkProviderServer)) return false;
			ChunkProviderServer chunkServer = (ChunkProviderServer)provider;
			
			Set<?> chunksToUnload          = PrivateFields.chunksToUnload.get(chunkServer);
			LongHashMap loadedChunkHashMap = PrivateFields.loadedChunkHashMap.get(chunkServer);
			List<Chunk> loadedChunks       = PrivateFields.loadedChunks.get(chunkServer);
			IChunkProvider chunkProvider   = PrivateFields.currentChunkProvider.get(chunkServer);
			
			int chunkCoordX = chunkCoords.getBlockX();
			int chunkCoordZ = chunkCoords.getBlockZ();
			
			if (chunkServer.chunkExists(chunkCoordX, chunkCoordZ))
			{
				chunkServer.loadChunk(chunkCoordX, chunkCoordZ).onChunkUnload();
			}
			
			long chunkIndex = ChunkCoordIntPair.chunkXZ2Int(chunkCoordX, chunkCoordZ);
			chunksToUnload.remove(chunkIndex);
			loadedChunkHashMap.remove(chunkIndex);
			Chunk chunk = chunkProvider.provideChunk(chunkCoordX, chunkCoordZ);
			loadedChunkHashMap.add(chunkIndex, chunk);
			loadedChunks.add(chunk);
			
			if (chunk != null)
			{
				try
				{
					chunk.onChunkLoad();
					chunk.populateChunk(chunkProvider, chunkProvider, chunkCoordX, chunkCoordZ);
				}
				catch (Exception ex)
				{
					for (BiomeGenBase biome : BiomeGenBase.getBiomeGenArray())
					{
						if (biome != null && biome.theBiomeDecorator != null)
							PrivateFields.currentWorld.set(biome.theBiomeDecorator, null);
					}
				}
				
				try
				{
					chunk.func_150809_p();
				}
				catch (Exception ex)
				{
					ex.printStackTrace(System.out);
				}
			}
		}
		catch (final Throwable th)
		{
			th.printStackTrace();
			return false;
		}
		
		return true;
	}

	/**
	 * @param chunkCoords
	 * @param oldChunkData
	 * @param editSession
	 * @param region
	 */
	public void applyChanges(final Vector chunkCoords, BaseBlock[] oldChunkData, EditSession editSession, Region region, World world)
	{
		PlayerManager playerManager = null;
		
		if (world instanceof WorldServer)
		{
			playerManager = ((WorldServer)world).getPlayerManager();
		}
		
		for (int x = 0; x < 16; x++)
		{
			for (int y = 0; y <= this.getMaxY(); y++)
			{
				for (int z = 0; z < 16; z++)
				{
					final Vector pt = chunkCoords.add(x, y, z);
					final int index = y * 16 * 16 + z * 16 + x;
					
					if (!region.contains(pt))
						editSession.smartSetBlock(pt, oldChunkData[index]);
					else
					{
						if (playerManager != null) playerManager.markBlockForUpdate(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
						editSession.rememberChange(pt, oldChunkData[index], editSession.rawGetBlock(pt));
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setBiome(com.sk89q.worldedit.Vector2D, com.sk89q.worldedit.BiomeType)
	 */
	@Override
	public void setBiome(Vector2D pt, BiomeType biome)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			BiomeGenBase biomeGen = VanillaBiomeTypes.getInstance().getFromBiomeType((VanillaBiomeType)biome);
			if (biomeGen == null) return;
			
			byte biomeID = (byte)biomeGen.biomeID;
			
			if (theWorld.getChunkProvider().chunkExists(pt.getBlockX() >> 4, pt.getBlockZ() >> 4))
			{
				Chunk chunk = theWorld.getChunkFromBlockCoords(pt.getBlockX(), pt.getBlockZ());
				if ((chunk != null) && (chunk.isChunkLoaded))
				{
					byte[] biomes = chunk.getBiomeArray();
					biomes[((pt.getBlockZ() & 0xF) << 4 | pt.getBlockX() & 0xF)] = biomeID;
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setBlock(com.sk89q.worldedit.Vector, com.sk89q.worldedit.foundation.Block, boolean)
	 */
	@SuppressWarnings("cast")
	@Override
	public boolean setBlock(Vector pt, Block block, boolean notify)
	{
		World theWorld = this.world.get();
		if (theWorld != null && block instanceof BaseBlock)
		{
			net.minecraft.block.Block newBlock = VanillaWorld.getBlockById(block.getId());
			boolean result = theWorld.setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newBlock);
			
			if (result && block instanceof TileEntityBlock)
			{
				this.copyToWorld(pt, (BaseBlock)block);
			}
			
			theWorld.setBlockMetadataWithNotify(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), block.getData(), 2);
			
			return result;
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setBlockData(com.sk89q.worldedit.Vector, int)
	 */
	@Override
	public void setBlockData(Vector pt, int data)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			theWorld.setBlockMetadataWithNotify(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data, 3);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setBlockDataFast(com.sk89q.worldedit.Vector, int)
	 */
	@Override
	public void setBlockDataFast(Vector pt, int data)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			theWorld.setBlockMetadataWithNotify(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), data, 3);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setBlockType(com.sk89q.worldedit.Vector, int)
	 */
	@Override
	public boolean setBlockType(Vector pt, int type)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			net.minecraft.block.Block newBlock = VanillaWorld.getBlockById(type);
			return theWorld.setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newBlock, 0, 3);
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setBlockTypeFast(com.sk89q.worldedit.Vector, int)
	 */
	@Override
	public boolean setBlockTypeFast(Vector pt, int type)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			net.minecraft.block.Block newBlock = VanillaWorld.getBlockById(type);
			return theWorld.setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newBlock);
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setTypeIdAndData(com.sk89q.worldedit.Vector, int, int)
	 */
	@Override
	public boolean setTypeIdAndData(Vector pt, int type, int data)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			net.minecraft.block.Block newBlock = VanillaWorld.getBlockById(type);
			return theWorld.setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newBlock, data, 3);
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#setTypeIdAndDataFast(com.sk89q.worldedit.Vector, int, int)
	 */
	@Override
	public boolean setTypeIdAndDataFast(Vector pt, int type, int data)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			net.minecraft.block.Block newBlock = VanillaWorld.getBlockById(type);
			return theWorld.setBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ(), newBlock, data, 3);
		}
		
		return false;
	}
	
	@Override
	public boolean generateTree(TreeType type, EditSession editSession, Vector pt) throws MaxChangedBlocksException
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			WorldGenerator treeGenerator = VanillaWorld.getTreeGeneratorByType(type, theWorld.rand);
			if (treeGenerator != null)
			{
				boolean result = treeGenerator.generate(new UndoWorldProxy(editSession, theWorld), theWorld.rand, pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
				return result;
			}
		}
		
		return false;
	}

	/**
	 * @param block
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static BaseBlock getBaseBlock(net.minecraft.block.Block block)
	{
		int type = net.minecraft.block.Block.getIdFromBlock(block);
		return new BaseBlock(type);
	}
	
	/**
	 * @param type
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static BaseBlock getBaseBlock(net.minecraft.block.Block block, int metaData)
	{
		int type = net.minecraft.block.Block.getIdFromBlock(block);
		return new BaseBlock(type, metaData);
	}

	/**
	 * @param pt
	 * @return
	 */
	private net.minecraft.block.Block getBlockAt(Vector pt)
	{
		World theWorld = this.world.get();
		if (theWorld != null)
		{
			return theWorld.getBlock(pt.getBlockX(), pt.getBlockY(), pt.getBlockZ());
		}
		
		return Blocks.air;
	}

	/**
	 * @param type
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static net.minecraft.block.Block getBlockById(int type)
	{
		final net.minecraft.block.Block block = net.minecraft.block.Block.getBlockById(type);
		return block != null ? block : Blocks.air;
	}
	
	/**
	 * @param block
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static int getIdFromBlock(net.minecraft.block.Block block)
	{
		return block == null ? 0 : net.minecraft.block.Block.getIdFromBlock(block);
	}
	
	/**
	 * @param item
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static ItemStack getItemStack(BaseItemStack item)
	{
		final ItemStack itemStack = new ItemStack(Item.getItemById(item.getType()), item.getAmount(), item.getData());
		for (final Map.Entry<Integer, Integer> entry : item.getEnchantments().entrySet())
		{
			itemStack.addEnchantment(net.minecraft.enchantment.Enchantment.enchantmentsList[entry.getKey().intValue()], entry.getValue().intValue());
		}
		
		return itemStack;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other)
	{
		if ((other instanceof VanillaWorld))
		{
			World otherWorld = ((VanillaWorld)other).world.get();
			return otherWorld != null ? otherWorld.equals(this.world.get()) : this.world.get() == null;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.blocks.LocalWorld#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return this.world.get() != null ? this.world.get().hashCode() : 0;
	}

	/**
	 * @param entity
	 * @param type
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private static boolean isEntityOfType(Entity entity, EntityType type)
	{
		switch (type)
		{
			case ALL:
				return (((entity instanceof EntityBoat)) || ((entity instanceof EntityItem)) || ((entity instanceof EntityFallingBlock)) || ((entity instanceof EntityMinecart)) || ((entity instanceof EntityHanging)) || ((entity instanceof EntityTNTPrimed)) || ((entity instanceof EntityXPOrb)) || ((entity instanceof EntityEnderEye)) || ((entity instanceof IProjectile)));
				
			case PROJECTILES:
			case ARROWS:
				return (((entity instanceof EntityEnderEye)) || ((entity instanceof IProjectile)));
				
			case BOATS:
				return ((entity instanceof EntityBoat));
				
			case ITEMS:
				return ((entity instanceof EntityItem));
				
			case FALLING_BLOCKS:
				return ((entity instanceof EntityFallingBlock));
				
			case MINECARTS:
				return ((entity instanceof EntityMinecart));
				
			case PAINTINGS:
				return ((entity instanceof EntityPainting));
				
			case ITEM_FRAMES:
				return ((entity instanceof EntityItemFrame));
				
			case TNT:
				return ((entity instanceof EntityTNTPrimed));
				
			case XP_ORBS:
				return ((entity instanceof EntityXPOrb));
		}
		
		return false;
	}

	/**
	 * @param type
	 * @param theWorld
	 * @return
	 */
	private static WorldGenerator getTreeGeneratorByType(TreeType type, Random rand)
	{
		switch (type)
		{
			case ACACIA:
				return new WorldGenSavannaTree(true);
			case BIG_TREE:
				return new WorldGenBigTree(true);
			case BIRCH:
				return new WorldGenForest(true, false);
			case BROWN_MUSHROOM:
				return new WorldGenBigMushroom(0);
			case DARK_OAK:
				return new WorldGenCanopyTree(true);
			case JUNGLE:
				return new WorldGenMegaJungle(true, 10, 20, 3, 3);
			case JUNGLE_BUSH:
				return new WorldGenShrub(3, 0);
			case MEGA_REDWOOD:
				return new WorldGenMegaPineTree(true, rand.nextBoolean());
			case REDWOOD:
				return new WorldGenTaiga2(true);
			case RED_MUSHROOM:
				return new WorldGenBigMushroom(1);
			case SMALL_JUNGLE:
				return new WorldGenTrees(true, 4 + rand.nextInt(7), 3, 3, false);
			case SWAMP:
				return new WorldGenSwamp();
			case TALL_BIRCH:
				return new WorldGenForest(true, true);
			case TALL_REDWOOD:
				return new WorldGenTaiga1();
			case TREE:
				return new WorldGenTrees(true);
		}
		
		return null;
	}
}