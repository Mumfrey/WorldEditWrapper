package com.mumfrey.worldeditwrapper.adapter;

import java.io.File;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.mumfrey.worldeditwrapper.asm.EventProxy;

/**
 * This adapter interface lets the main mod keep the wrapper itself at arms' length, this allows us
 * to gracefully fail if the wrapper doesn't load (eg. because WorldEdit didn't get injected properly)
 * and not crash the client.
 * 
 * @author Adam Mummery-Smith
 */
public interface IWorldEditWrapper
{
	/**
	 * Get plugin channels required by the wrapper
	 */
	public abstract List<String> getChannels();
	
	/**
	 * Initialise the wrapper
	 * 
	 * @param configPath
	 */
	public abstract void init(File configPath);

	/**
	 * Get the WorldEdit version from the metadata
	 * 
	 * @return
	 */
	public abstract String getWorldEditVersion();
	
	/**
	 * 
	 */
	public abstract void onTick();
	
	/**
	 * @param player
	 * @param message
	 * @return
	 */
	public abstract boolean onChat(EntityPlayerMP player, String message);
	
	/**
	 * @param sender
	 * @param channel
	 * @param data
	 */
	public abstract void onCustomPayload(EntityPlayerMP sender, String channel, byte[] data);
	
	/**
	 * @param server
	 */
	public abstract void onJoinWorld(MinecraftServer server);
	
	/**
	 * @param action
	 * @param player
	 * @param x
	 * @param y
	 * @param z
	 * @param side
	 * @return
	 */
	public abstract boolean onPlayerInteract(EventProxy.Action action, EntityPlayerMP player, int x, int y, int z, int side);
}