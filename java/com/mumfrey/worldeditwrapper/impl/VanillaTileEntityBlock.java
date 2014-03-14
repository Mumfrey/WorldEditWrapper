package com.mumfrey.worldeditwrapper.impl;

import java.lang.reflect.Constructor;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Block with tile entity impl
 * 
 * @author Adam Mummery-Smith
 */
public class VanillaTileEntityBlock extends BaseBlock
{
	/**
	 * Wrapped TE
	 */
	private TileEntity tileEntity;
	
	/**
	 * @param type
	 * @param data
	 * @param tileEntity
	 */
	public VanillaTileEntityBlock(int type, int data, TileEntity tileEntity)
	{
		super(type, data);
		
		this.tileEntity = tileEntity;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.foundation.Block#getNbtId()
	 */
	@Override
	public String getNbtId()
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		try
		{
			this.tileEntity.writeToNBT(tag);
			return tag.getString("id");
		}
		catch (final Exception e) {}
		
		return "";
	}
	
	/**
	 * @return
	 */
	public TileEntity getTileEntity()
	{
		return this.tileEntity;
	}
	
	/**
	 * @param tileEntity
	 * @return
	 */
	public VanillaTileEntityBlock setTileEntity(TileEntity tileEntity)
	{
		this.tileEntity = tileEntity;
		return this;
	}

	/**
	 * @param blockPosition
	 * @return
	 */
	public TileEntity createCopyAt(Vector blockPosition)
	{
		try
		{
			Class<? extends TileEntity> blockClass = this.tileEntity.getClass();
			Constructor<? extends TileEntity> tileEntityCtor = blockClass.getConstructor();
			TileEntity newTileEntity = tileEntityCtor.newInstance();
			newTileEntity.readFromNBT(this.getTileEntityData(blockPosition));
			return newTileEntity;
		}
		catch (Throwable th) {}
		
		return null;
	}
	
	/**
	 * @param vec
	 * @return
	 */
	private NBTTagCompound getTileEntityData(Vector vec)
	{
		NBTTagCompound tag = new NBTTagCompound();
		
		this.tileEntity.writeToNBT(tag);
		
		tag.setTag("x", new NBTTagInt(vec.getBlockX()));
		tag.setTag("y", new NBTTagInt(vec.getBlockY()));
		tag.setTag("z", new NBTTagInt(vec.getBlockZ()));
		
		return tag;
	}
}