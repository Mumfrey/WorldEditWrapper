package com.mumfrey.worldeditwrapper.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

import com.google.common.base.Joiner;
import com.mumfrey.liteloader.core.PluginChannels.ChannelPolicy;
import com.mumfrey.liteloader.core.ServerPluginChannels;
import com.mumfrey.worldeditwrapper.reflect.PrivateFields;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.cui.CUIEvent;

/**
 * Server interface impl for vanilla
 * 
 * @author Adam Mummery-Smith
 */
public class VanillaServerInterface extends ServerInterface
{
	private final Map<String, VanillaPlayer> players = new HashMap<String, VanillaPlayer>();
	
	/**
	 * Server 
	 */
	private final MinecraftServer server;
	
	/**
	 * Scheduled tasks pool
	 */
	private final List<WorldEditScheduledTask> tasks = new ArrayList<WorldEditScheduledTask>();
	
	/**
	 * @param server
	 */
	public VanillaServerInterface(MinecraftServer server)
	{
		this.server = server;
	}
	
	/**
	 * @param player
	 * @return
	 */
	public VanillaPlayer getLocalPlayer(EntityPlayerMP player)
	{
		String playerName = player.getCommandSenderName();
		VanillaPlayer localPlayer = this.players.get(playerName);
		
		if (localPlayer != null && localPlayer.getPlayer() != player)
		{
			localPlayer = null;
		}
		
		if (localPlayer == null)
		{
			localPlayer = new VanillaPlayer(this, player);
			this.players.put(playerName, localPlayer);
		}
		
		return localPlayer;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.ServerInterface#getBiomes()
	 */
	@Override
	public BiomeTypes getBiomes()
	{
		return VanillaBiomeTypes.getInstance();
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.ServerInterface#getWorlds()
	 */
	@Override
	public List<LocalWorld> getWorlds()
	{
		final List<LocalWorld> worlds = new ArrayList<LocalWorld>(this.server.worldServers.length);
		
		for (WorldServer world : this.server.worldServers)
			worlds.add(VanillaWorld.getLocalWorld(world));
		
		return worlds;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.ServerInterface#reload()
	 */
	@Override
	public void reload()
	{
		this.tasks.clear();
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.ServerInterface#onCommandRegistration(java.util.List)
	 */
	@Override
	public void onCommandRegistration(List<Command> commands)
	{
		if (this.server != null)
		{
			CommandHandler serverCommandManager = (CommandHandler)this.server.getCommandManager();
			
			for (Command command : commands)
			{
				serverCommandManager.registerCommand(new WorldEditCommand(command));
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.ServerInterface#isValidMobType(java.lang.String)
	 */
	@Override
	public boolean isValidMobType(String type)
	{
		try
		{
			return PrivateFields.StaticFields.stringToClassMapping.get().containsKey(type);
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.ServerInterface#resolveItem(java.lang.String)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "deprecation" })
	public int resolveItem(String name)
	{
		if (name == null) return 0;
		
		final Iterator<Item> itemIter = Item.itemRegistry.iterator();
		while (itemIter.hasNext())
		{
			Item item = itemIter.next();
			String itemName = item.getUnlocalizedName();

			if (itemName != null)
			{
				int dotPos = itemName.indexOf('.');
				if (dotPos > 0) itemName = itemName.substring(dotPos);
				
				if (name.equalsIgnoreCase(itemName))
					return Item.getIdFromItem(item);
			}
		}
		
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see com.sk89q.worldedit.ServerInterface#schedule(long, long, java.lang.Runnable)
	 */
	@Override
	public int schedule(long delay, long period, Runnable task)
	{
		WorldEditScheduledTask scheduledTask = new WorldEditScheduledTask(task, period, delay);
		this.tasks.add(scheduledTask);
		return this.tasks.size() - 1;
	}
	
	/**
	 * For scheduler
	 */
	public void onTick()
	{
		for (WorldEditScheduledTask task : this.tasks)
		{
			task.onTick();
		}
	}

	public void dispatchCUIEvent(VanillaPlayer localPlayer, CUIEvent event)
	{
		String message = VanillaServerInterface.packCUIEvent(event);
		ServerPluginChannels.sendMessage(localPlayer.getPlayer(), WorldEditWrapper.WECUI_CHANNEL, message.getBytes(WorldEditWrapper.UTF8), ChannelPolicy.DISPATCH_ALWAYS);
	}

	/**
	 * @param event
	 * @return
	 */
	private static String packCUIEvent(CUIEvent event)
	{
		String message = event.getTypeId();
		String[] params = event.getParameters();
		
		if (params.length > 0)
		{
			return message + "|" + Joiner.on('|').join(params);
		}
		
		return message;
	}
}
