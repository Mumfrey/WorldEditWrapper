package com.mumfrey.worldeditwrapper.impl;

import java.lang.ref.WeakReference;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIEvent;

/**
 * LocalPlayer impl
 * 
 * @author Adam Mummery-Smith
 */
public class VanillaPlayer extends LocalPlayer
{
	private static final String CHATPREFIX = ""; // EnumChatFormatting.WHITE + "[" + EnumChatFormatting.DARK_RED + "WorldEdit" + EnumChatFormatting.WHITE + "] ";

	/**
	 * Player we're wrapping
	 */
	private final WeakReference<EntityPlayerMP> player;
	
	/**
	 * Player's name
	 */
	private final String playerName;
	
	/**
	 * @param server
	 * @param player
	 */
	VanillaPlayer(VanillaServerInterface server, EntityPlayerMP player)
	{
		super(server);
		
		this.player = new WeakReference<EntityPlayerMP>(player);
		this.playerName = player.getCommandSenderName();
	}
	
	/**
	 * @return
	 */
	public EntityPlayerMP getPlayer()
	{
		return this.player.get();
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getName()
	 */
	@Override
	public String getName()
	{
		return this.playerName;
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getGroups()
	 */
	@Override
	public String[] getGroups()
	{
		return new String[] {};
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getInventoryBlockBag()
	 */
	@Override
	public BlockBag getInventoryBlockBag()
	{
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getItemInHand()
	 */
	@SuppressWarnings("deprecation")
	@Override
	public int getItemInHand()
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			ItemStack stack = thePlayer.getCurrentEquippedItem();
			if (stack != null)
			{
				Item item = stack.getItem();
				if (item != null) return Item.getIdFromItem(item);
			}
		}
		
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getPitch()
	 */
	@Override
	public double getPitch()
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			return thePlayer.rotationPitch;
		}
		
		return 0.0;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getYaw()
	 */
	@Override
	public double getYaw()
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			return thePlayer.rotationYaw;
		}
		
		return 0.0;
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getPosition()
	 */
	@Override
	public WorldVector getPosition()
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			return new WorldVector(VanillaWorld.getLocalWorld(thePlayer.worldObj), thePlayer.posX, thePlayer.posY, thePlayer.posZ);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#setPosition(com.sk89q.worldedit.Vector, float, float)
	 */
	@Override
	public void setPosition(Vector pos, float pitch, float yaw)
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			thePlayer.playerNetServerHandler.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), pitch, yaw);
		}
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#getWorld()
	 */
	@Override
	public LocalWorld getWorld()
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			return VanillaWorld.getLocalWorld(thePlayer.worldObj);
		}
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#giveItem(int, int)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void giveItem(int type, int amt)
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			Item item = Item.getItemById(type);
			if (item != null)
			{
				ItemStack stack = new ItemStack(item, amt, 0);
				thePlayer.inventory.addItemStackToInventory(stack);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#hasPermission(java.lang.String)
	 */
	@Override
	public boolean hasPermission(String perm)
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			Minecraft minecraft = Minecraft.getMinecraft();
			return (minecraft.isSingleplayer() && minecraft.getIntegratedServer().getServerOwner().equals(this.playerName)) || minecraft.getIntegratedServer().getConfigurationManager().isPlayerOpped(this.playerName);
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#print(java.lang.String)
	 */
	@Override
	public void print(String msg)
	{
		for (String line : msg.split("\n"))
		{
			this.sendMessage(CHATPREFIX + EnumChatFormatting.LIGHT_PURPLE + line);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#printDebug(java.lang.String)
	 */
	@Override
	public void printDebug(String msg)
	{
		for (String line : msg.split("\n"))
		{
			this.sendMessage(CHATPREFIX + EnumChatFormatting.GRAY + line);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#printError(java.lang.String)
	 */
	@Override
	public void printError(String msg)
	{
		for (String line : msg.split("\n"))
		{
			this.sendMessage(CHATPREFIX + EnumChatFormatting.RED + line);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#printRaw(java.lang.String)
	 */
	@Override
	public void printRaw(String msg)
	{
		for (String line : msg.split("\n"))
		{
			this.sendMessage(CHATPREFIX + line);
		}
	}

	/**
	 * Send a chat message to the local player
	 * 
	 * @param message
	 */
	private void sendMessage(String message)
	{
		EntityPlayerMP thePlayer = this.player.get();
		if (thePlayer != null)
		{
			thePlayer.addChatComponentMessage(new ChatComponentText(message));
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.LocalPlayer#dispatchCUIEvent(com.sk89q.worldedit.cui.CUIEvent)
	 */
	@Override
	public void dispatchCUIEvent(CUIEvent event)
	{
		((VanillaServerInterface)this.server).dispatchCUIEvent(this, event);
	}
}